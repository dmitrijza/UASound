package org.uamusic.bot.telegram;

import it.tdlight.client.SimpleTelegramClient;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.uamusic.bot.telegram.chat.commands.inline.InlineProcessor;
import org.uamusic.bot.telegram.chat.commands.inline.SimpleInlineProcessor;
import org.uamusic.data.service.PG.PGDataService;
import org.uamusic.bot.telegram.chat.export.bot.ExportIntegrationService;
import org.uamusic.bot.telegram.chat.export.bot.TelegramIntegrationService;
import org.uamusic.bot.telegram.chat.export.selfbot.ExportService;
import org.uamusic.bot.telegram.chat.export.selfbot.SelfBotExportService;
import org.uamusic.bot.telegram.chat.export.selfbot.SelfBotSpecies;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Getter
public class TelegramBot extends TelegramLongPollingCommandBot {
    private final PGDataService dataService = new PGDataService();

    private final ExportService exportService = new SelfBotExportService(this);
    private final ExportIntegrationService integrationService = new TelegramIntegrationService(this);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final InlineProcessor inlineProcessor = new SimpleInlineProcessor();

    @Setter
    private Consumer<List<Update>> updatesHandler = null;

    static SimpleTelegramClient client;

    static final Logger _LOGGER = LoggerFactory.getLogger(TelegramBot.class);

    static {
        try {
            client = SelfBotSpecies.authorize();
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

        executorService.submit(() -> {
            this.inlineProcessor.process(this, update, update.getInlineQuery());
        });
    }

    public SimpleTelegramClient getTelegramClient(){
        return client;
    }

    public PGDataService getDataService() {
        return dataService;
    }
}
