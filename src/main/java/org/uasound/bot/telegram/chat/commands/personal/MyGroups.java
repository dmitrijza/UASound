package org.uasound.bot.telegram.chat.commands.personal;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.DefaultBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.uasound.bot.telegram.TelegramBot;
import org.uasound.data.entity.GroupCard;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MyGroups extends DefaultBotCommand {

    private final TelegramBot instance;

    public MyGroups(final TelegramBot instance) {
        super("my_groups", "Переглянути список індексованих груп");

        this.instance = instance;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        final User user = message.getFrom();



    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, Integer messageId, String[] arguments) {
        // ...
    }
}
