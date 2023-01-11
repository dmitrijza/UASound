package org.uasound.bot.telegram.chat.commands;

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
                "\n\n" +
                ">Але ж це неправильно, що бот допомагає поширювати піратський контент, чи не так ?.." +
                "\n\n" +
                "<i>Бот лише індексує вже наявні в Телеграмі дані, до того ж (часто) не найкращої якості. Ми не " +
                "поширюємо піратський контент, але поширюємо загальнодоступну інформацію українською. \n" +
                "Тому, як адміністрація бота, ми рекомендуємо все ж купувати (в т.ч. Digital) альбоми авторів, " +
                "яких ви шукатимете тут. Крім того, підтримка українських авторів - наш з вами спільний обов'язок.</i>" +
                "\n\n" +
                "<i><a href='https://savelife.in.ua'>Підтримати армію незалежної України.</a></i>\n" +
                "<i><b>За підтримки <a href='https://t.me/+g82l_SaAEek2MmZi'>Український метал і рок ✙ #УкрТґ</a></b></i>";

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
