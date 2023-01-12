package org.uasound.bot.telegram;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.uasound.bot.telegram.chat.commands.AlbumSearch;
import org.uasound.bot.telegram.chat.commands.SearchAuthor;
import org.uasound.bot.telegram.chat.commands.StartCommand;
import org.uasound.bot.telegram.chat.commands.personal.MyGroups;
import org.uasound.bot.telegram.chat.export.bot.ExportIntegrationService;
import org.uasound.bot.telegram.chat.export.bot.TelegramBotAdapter;
import org.uasound.bot.telegram.chat.export.selfbot.ExportService;
import org.uasound.data.service.DataService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.uasound.bot.telegram.TelegramBot._LOGGER;

@Configuration
@ComponentScan(basePackageClasses = TelegramBotAdapter.class)
public class TelegramConfiguration {

    private DataService dataService;

    private ExportIntegrationService integrationService;

    private ExportService exportService;

    private final TelegramBotAdapter telegramBotAdapter;

    @Autowired
    public TelegramConfiguration(final DataService dataService,
                                 final ExportIntegrationService integrationService,
                                 final ExportService exportService,
                                 final TelegramBotAdapter telegramBotAdapter){
        this.dataService = dataService;
        this.integrationService = integrationService;
        this.exportService = exportService;
        this.telegramBotAdapter = telegramBotAdapter;
    }

    @PostConstruct @Async
    public void postConstruct(){
        try {
            final TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);

            this.telegramBotAdapter.registerAll(new StartCommand());
            this.telegramBotAdapter.registerAll(new SearchAuthor(telegramBotAdapter));
            this.telegramBotAdapter.registerAll(new AlbumSearch(telegramBotAdapter));
            this.telegramBotAdapter.registerAll(new MyGroups(telegramBotAdapter));

            api.registerBot(this.telegramBotAdapter);

            _LOGGER.info("Initialized.");

            integrationService.init();
            exportService.init();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Telegram");
        executor.initialize();

        return executor;
    }
}
