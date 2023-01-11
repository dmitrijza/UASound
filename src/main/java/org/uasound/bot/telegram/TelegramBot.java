package org.uasound.bot.telegram;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.uasound.bot.telegram.chat.commands.inline.InlineProcessor;
import org.uasound.bot.telegram.chat.commands.inline.SimpleInlineProcessor;
import org.uasound.bot.telegram.chat.export.bot.ExportIntegrationService;
import org.uasound.bot.telegram.chat.export.bot.TelegramIntegrationService;
import org.uasound.bot.telegram.chat.export.selfbot.ExportService;
import org.uasound.bot.telegram.chat.export.selfbot.SelfBotAdapter;
import org.uasound.bot.telegram.chat.export.selfbot.SelfBotExportService;
import org.uasound.bot.telegram.chat.export.selfbot.SelfBotSpecies;
import org.uasound.data.service.DataService;
import org.uasound.data.service.PG.PGDataService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Getter
public class TelegramBot extends TelegramLongPollingCommandBot {
    private final DataService dataService = new PGDataService();
    private final ExportService exportService = new SelfBotExportService(this);
    private final ExportIntegrationService integrationService = new TelegramIntegrationService(this);
    private final InlineProcessor inlineProcessor = new SimpleInlineProcessor();

    public static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(3);

    @Setter
    private Consumer<List<Update>> updatesHandler = null;

    static SelfBotAdapter client;

    public static final Logger _LOGGER = LoggerFactory.getLogger(TelegramBot.class);

    static {
        try {
            client = new SelfBotAdapter(SelfBotSpecies.authorize());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public TelegramBot(){
        this.dataService.init();
        this.exportService.init();
        this.integrationService.init();
    }

    @Override
    public String getBotUsername() {
        return "UASound";
    }
    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
    }

    public void schedule(final Runnable task, final long delay, final TimeUnit unit){
        EXECUTOR.scheduleAtFixedRate(task, 0, delay, unit);
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        if (this.updatesHandler != null)
            this.updatesHandler.accept(updates);

        super.onUpdatesReceived(updates);
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (!(update.hasInlineQuery()))
            return;

        EXECUTOR.submit(() -> {
            this.inlineProcessor.process(this, update, update.getInlineQuery());
        });
    }

    public SelfBotAdapter getTelegramClient(){
        return client;
    }

    public DataService getDataService() {
        return dataService;
    }
}
