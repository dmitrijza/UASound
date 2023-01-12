package org.uasound.bot.telegram;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.uasound.bot.telegram.chat.export.bot.TelegramBotAdapter;
import org.uasound.bot.telegram.chat.export.selfbot.SelfBotAdapter;
import org.uasound.bot.telegram.chat.export.selfbot.SelfBotSpecies;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@ComponentScan(
        basePackages = {
                "org.uasound.bot.telegram.chat.export.selfbot",
                "org.uasound.bot.telegram.chat.export.bot",
                "org.uasound.data.service.hibernate"
        },
        basePackageClasses = {
                TelegramConfiguration.class,
                TelegramBotAdapter.class,
                SelfBotAdapter.class
        }
)
@Getter
public class TelegramBot {
    static SelfBotAdapter client;

    static TelegramBotAdapter botAdapter;

    public static final Logger _LOGGER = LoggerFactory.getLogger(TelegramBot.class);

    public static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(3);

    public void schedule(final Runnable task, final long delay, final TimeUnit unit){
        EXECUTOR.scheduleAtFixedRate(task, 0, delay, unit);
    }

    public void submit(Runnable task) {
        EXECUTOR.submit(task);
    }

    @Bean @Lazy
    public SelfBotAdapter getTelegramClient() throws InterruptedException {
        return new SelfBotAdapter(SelfBotSpecies.authorize());
    }
}
