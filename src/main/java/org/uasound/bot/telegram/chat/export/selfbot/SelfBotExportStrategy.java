package org.uasound.bot.telegram.chat.export.selfbot;

import it.tdlight.jni.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uasound.data.entity.DerivedData;
import org.uasound.data.entity.DerivedMeta;
import org.uasound.bot.telegram.chat.export.selfbot.strategy.ExportStrategy;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SelfBotExportStrategy implements ExportStrategy<TdApi.Message> {
    static final String _SCHEMA_VERSION = "0.1b";

    static final Logger _LOGGER = LoggerFactory.getLogger(SelfBotExportStrategy.class);

    @Override
    public void transform(TdApi.Message input,
                          Consumer<DerivedData> consumer,
                          final BiConsumer<TdApi.Message, Exception> exceptionHandler)
    {
        consumer.accept(this.wrap(input, exceptionHandler));
    }

    @Override
    public DerivedData wrap(TdApi.Message rawInput, final BiConsumer<TdApi.Message, Exception> exceptionHandler) {
        if (rawInput.content == null) {
            _LOGGER.debug("{}: no content.", rawInput.id);
            return null;
        }

        boolean isRepost = rawInput.forwardInfo != null;

        if (isRepost){
            return null;
        }

        if (!(rawInput.content instanceof TdApi.MessageAudio)) {
            _LOGGER.debug("{}: no audio.", rawInput.id);
            return null;
        }

        final TdApi.MessageAudio messageAudio = (TdApi.MessageAudio) rawInput.content;
        final TdApi.Audio audio = messageAudio.audio;
        final TdApi.File file = audio.audio;

        final Timestamp timestamp = Timestamp.from(Instant.now());

        final TdApi.Minithumbnail albumCoverMini = audio.albumCoverMinithumbnail;
        final TdApi.Thumbnail albumCoverLarge = audio.albumCoverThumbnail;

        _LOGGER.info("{} : {}", messageAudio.caption, file.remote.id);

        return DerivedData.builder()
                .creationTimestamp(timestamp)
                .modificationTimestamp(timestamp)
                .groupId(rawInput.chatId)
                .postId(rawInput.id)
                .fileId(String.valueOf(file.id))
                .fileUniqueId(file.remote.uniqueId)
                .remoteFileId(file.remote.id)
                .trackName(audio.title)
                .trackDuration(String.valueOf(audio.duration))
                .trackPerformer(audio.performer)
                .schema(_SCHEMA_VERSION)
                .derivedMeta(DerivedMeta.builder()
                        .meta(new HashMap<>(Map.ofEntries(
                                Map.entry(SelfBotExportService.MetaKeys.DURATION.getKey(),
                                        Integer.toString(audio.duration)),
                                Map.entry(SelfBotExportService.MetaKeys.TITLE.getKey(),
                                        audio.title),
                                Map.entry(SelfBotExportService.MetaKeys.PERFORMER.getKey(),
                                        audio.performer),
                                Map.entry(SelfBotExportService.MetaKeys.FILE_NAME.getKey(),
                                        audio.fileName),
                                Map.entry(SelfBotExportService.MetaKeys.MIME_TYPE.getKey(),
                                        audio.mimeType),
                                Map.entry(SelfBotExportService.MetaKeys.ALBUM_COVER_LARGE.getKey(),
                                        albumCoverMini != null ? albumCoverMini.toString() : "null"),
                                Map.entry(SelfBotExportService.MetaKeys.ALBUM_COVER_MINI.getKey(),
                                        albumCoverLarge != null ? albumCoverLarge.toString() : "null"),
                                Map.entry(SelfBotExportService.MetaKeys.AUDIO.getKey(),
                                        audio.audio.toString()),
                                Map.entry(SelfBotExportService.MetaKeys.EDIT_DATE.getKey(),
                                        Integer.toString(rawInput.editDate)),
                                Map.entry(SelfBotExportService.MetaKeys.DATE.getKey(),
                                        Integer.toString(rawInput.date)),
                                Map.entry(SelfBotExportService.MetaKeys.EXTERNAL_ALBUM_COVERS.getKey(),
                                        Arrays.toString(audio.externalAlbumCovers)))))
                        .schema(_SCHEMA_VERSION)
                        .build())
                .aggregator(SelfBotExportService.class.getCanonicalName())
                .build();
    }
}
