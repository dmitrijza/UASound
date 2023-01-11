package org.uasound.bot.telegram;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Setter
    private Consumer<List<Update>> updatesHandler = null;

    private final Map<Long, Consumer<Update>> contextActionHandlers = new HashMap<>();

    static SelfBotAdapter client;

    public static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(3);
    public static final Logger _LOGGER = LoggerFactory.getLogger(TelegramBot.class);

    static {
        try {
            client = new SelfBotAdapter(SelfBotSpecies.authorize());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public TelegramBot(){
        try {
            this.dataService.init();
            this.exportService.init();
            this.integrationService.init();
        } catch (Throwable e){
            _LOGGER.error("Can't initialize bot context", e);
        }
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
        final Message message = update.getMessage();

        if (!(update.hasInlineQuery()) && message != null){
            final User user = message.getFrom();

            if (user == null)
                return;

            if (contextActionHandlers.containsKey(user.getId())) {
                final Consumer<Update> consumer = contextActionHandlers.get(user.getId());

                contextActionHandlers.remove(user.getId());
                consumer.accept(update);
            }
        }

        if (!(update.hasInlineQuery()))
            return;

        EXECUTOR.submit(() -> {
            this.inlineProcessor.process(this, update, update.getInlineQuery());
        });
    }

    public void await(final User user, final Consumer<Update> updateConsumer){
        this.contextActionHandlers.put(user.getId(), updateConsumer);
    }

    public SelfBotAdapter getTelegramClient(){
        return client;
    }

    public DataService getDataService() {
        return dataService;
    }
}
