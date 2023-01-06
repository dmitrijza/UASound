package org.uamusic;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.uamusic.bot.commands.StartCommand;
import org.uamusic.bot.telegram.TelegramBot;

import java.util.List;

public class Starter {
    public static void main(String[] args) throws Exception {
        final TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        final TelegramBot bot = new TelegramBot();

        new Thread(() -> {
            try {
                api.registerBot(bot);

                bot.register(new StartCommand());
                bot.execute(SetMyCommands.builder()
                        .commands(List.of(BotCommand.builder()
                                .command("start")
                                .description("Показати початкову інформацію")
                                .build())
                        )
                        .build());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}