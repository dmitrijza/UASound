package org.uasound.bot.telegram.chat.export.bot;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.uasound.bot.telegram.chat.commands.inline.InlineProcessor;
import org.uasound.bot.telegram.chat.commands.inline.SimpleInlineProcessor;
import org.uasound.data.service.DataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
@Getter
public class TelegramBotAdapter extends TelegramLongPollingCommandBot {
    private final IntegrationUpdatesHandler updatesHandler;

    private final DataService dataService;

    private final ExportIntegrationService integrationService;

    private final Map<Long, Consumer<Update>> contextActionHandlers = new HashMap<>();

    private final InlineProcessor inlineProcessor = new SimpleInlineProcessor();

    static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    @Autowired
    public TelegramBotAdapter(final DataService dataService,
                              final ExportIntegrationService integrationService,
                              final IntegrationUpdatesHandler updatesHandler)
    {
        this.dataService = dataService;
        this.integrationService = integrationService;
        this.updatesHandler = updatesHandler;
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
            this.inlineProcessor.process(this, dataService, update, update.getInlineQuery());
        });
    }

    public void await(final User user, final Consumer<Update> updateConsumer){
        this.contextActionHandlers.put(user.getId(), updateConsumer);
    }

}
