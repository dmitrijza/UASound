package org.uamusic.bot.telegram.chat.commands;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.DefaultBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class StartCommand extends DefaultBotCommand {
    public StartCommand() {
        super("start", "Показати початкову інформацію");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, Integer messageId, String[] arguments) {
        final String text = "" +
                "\n" +
                ">Що таке <b>UAMusic?</b>\n" +
                "\n" +
                "<i>UAMusic - це перший український бот, який збирає й структурує аудіо-матеріали" +
                " українською, а також впроваджує багатий функціонал у звичайні чати.</i>" +
                "\n" +
                "";

        try {
            absSender.execute(SendMessage.builder()
                            .chatId(chat.getId())
                            .replyToMessageId(messageId)
                            .text(text)
                            .parseMode("HTML")
                            .disableWebPagePreview(false)
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
