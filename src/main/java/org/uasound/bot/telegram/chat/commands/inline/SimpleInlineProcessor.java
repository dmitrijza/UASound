package org.uasound.bot.telegram.chat.commands.inline;

import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.cached.InlineQueryResultCachedAudio;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.uasound.bot.telegram.TelegramBot;
import org.uasound.data.entity.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class SimpleInlineProcessor implements InlineProcessor {

    static final String INLINE_TEMPLATE = "" +
            "%s <a href = 'https://t.me/%s'><b>%s</b></a> ";

    static final String INLINE_TEMPLATE_LINKED = "" +
            "%s <a href = 'https://t.me/%s'><b>%s</b></a> \n" +
            "\uD83D\uDCBD <i>Альбом: %s</i>";

    @Override
    public void process(TelegramBot bot, Update update, InlineQuery query) {
        final String rawQuery = query.getQuery().replace(" ", "");

        final Collection<DerivedData> coincidences = bot.getDataService().search(rawQuery);
        final List<SharedAudio> audios = new ArrayList<>();

        coincidences.forEach((data) -> {
            bot.getIntegrationService().integrate(data, audios::add);

            if (audios.size() == coincidences.size() || audios.size() == 10){
                try {
                    bot.execute(AnswerInlineQuery.builder()
                            .inlineQueryId(query.getId())
                            .results(audios.stream()
                                    .map((audio) -> {
                                        final GroupCard card = bot.getDataService().getGroupCard(data.getGroupId());
                                        final AlbumLinkage linkage = bot.getDataService().getLinkageOf(audio.getInternalId());

                                        DerivedAlbum album = null;

                                        if (linkage != null)
                                            album = bot.getDataService().getAlbum(linkage.getAlbumId());

                                        String caption = linkage == null ?
                                                String.format(
                                                        INLINE_TEMPLATE,
                                                        card.getGroupPrefix(),
                                                        card.getGroupInviteId() == null ?
                                                                card.getGroupTag() : card.getGroupInviteId(),
                                                        card.getGroupTitle())
                                                 : String.format(
                                                INLINE_TEMPLATE_LINKED,
                                                card.getGroupPrefix(),
                                                card.getGroupInviteId() == null ?
                                                        card.getGroupTag() : card.getGroupInviteId(),
                                                card.getGroupTitle(), album.getAuthor() + " — <b>" + album.getName() + "</b>");

                                        return InlineQueryResultCachedAudio.builder()
                                                .id(String.valueOf(ThreadLocalRandom.current().nextInt()))
                                                .audioFileId(audio.getFileId())
                                                .caption(caption)
                                                .parseMode("HTML")
                                                .build();
                                    })
                                    .collect(Collectors.toList()))
                            .build());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
