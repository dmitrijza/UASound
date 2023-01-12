package org.uasound.bot.telegram.chat;

import lombok.Getter;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.DefaultBotCommand;
import org.uasound.bot.telegram.TelegramBot;
import org.uasound.bot.telegram.chat.export.bot.TelegramBotAdapter;

@Getter
public abstract class BotDependentCommand extends DefaultBotCommand {
    private final TelegramBotAdapter instance;

    public BotDependentCommand(final TelegramBotAdapter bot, String commandIdentifier, String description) {
        super(commandIdentifier, description);

        this.instance = bot;
    }
}
