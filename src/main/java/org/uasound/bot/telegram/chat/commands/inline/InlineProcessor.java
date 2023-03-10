package org.uasound.bot.telegram.chat.commands.inline;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.uasound.bot.telegram.TelegramBot;

public interface InlineProcessor {

    void process(final TelegramBot bot, Update update, final InlineQuery query);

}
