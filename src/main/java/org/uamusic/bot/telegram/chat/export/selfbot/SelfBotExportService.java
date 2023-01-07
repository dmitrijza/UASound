package org.uamusic.bot.telegram.chat.export.selfbot;

import it.tdlight.client.Result;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uamusic.bot.telegram.TelegramBot;
import org.uamusic.data.entity.DerivedData;
import org.uamusic.bot.telegram.chat.export.selfbot.strategy.ExportStrategy;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The synchronous export service that provides a way
 * to save audio from certain (=pre-defined) channels.
 */

@RequiredArgsConstructor
public class SelfBotExportService implements ExportService {
    static final Logger _LOGGER = LoggerFactory.getLogger(SelfBotExportService.class);
    enum MetaKeys {

        DURATION("duration"),
        TITLE("title"),
        PERFORMER("performer"),
        FILE_NAME("fileName"),
        MIME_TYPE("mimeType"),

        /**
         * Minithumbnail structure.
         * @see "https://core.telegram.org/tdlib/docs/classtd_1_1td__api_1_1minithumbnail.html"
         */
        ALBUM_COVER_MINI("albumCoverMinithumbnail"),

        /**
         * Thumbnail structure.
         * @see "https://core.telegram.org/tdlib/docs/classtd_1_1td__api_1_1thumbnail.html"
         */
        ALBUM_COVER_LARGE("albumCoverThumbnail"),

        EXTERNAL_ALBUM_COVERS("externalAlbumCovers"),

        /**
         * Audio structure.
         * @see "https://core.telegram.org/tdlib/docs/classtd_1_1td__api_1_1file.html"
         */
        AUDIO("audio"),

        EDIT_DATE("edit_date"),

        DATE("date");

        private final String parent;


        MetaKeys(final String parent) {
            this.parent = parent;
        }
        public String getKey() {
            return parent;
        }

    }

    static final int _TELEGRAM_THRESHOLD = 5;

    static final ScheduledExecutorService _EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    private final TelegramBot botInstance;

    private SimpleTelegramClient telegramClient;
    private final ExportStrategy<TdApi.Message> strategy = new SelfBotExportStrategy();
    static long _DATA_POINTER = 0;

    @Override
    public void init() {
        telegramClient = botInstance.getTelegramClient();

        Runtime.getRuntime().addShutdownHook(new Thread(_EXECUTOR::shutdown));
    }

    @Override
    public void scheduleExport(String groupTag, long time, TimeUnit unit) {
        _EXECUTOR.scheduleAtFixedRate(() ->
                _LOGGER.info("Scheduled new export from {}", groupTag), 0L, time, unit);
    }

    public void export(final String groupTag) {
        _EXECUTOR.submit(() ->
                telegramClient.send(new TdApi.SearchChats(groupTag, 1), (rs) -> {
            final TdApi.Chats chats = rs.get();
            final long groupId = chats.chatIds[0];

            telegramClient.send(
                    new TdApi.GetChatHistory(groupId, _DATA_POINTER, 0, 1, false),
                    (res) -> proceed(telegramClient, botInstance, groupTag, groupId, res)
            );
        }));
    }

    public void proceed(final SimpleTelegramClient client,
                        final TelegramBot bot,
                        final String groupTag,
                        final long groupId,
                        final Result<TdApi.Messages> res) {
        final TdApi.Messages messages = res.get();

        if (messages.messages.length == 0){
            _LOGGER.info("Export for group ({}:{}) done.", groupTag, groupId);
            return;
        }

        final TdApi.Message last = messages.messages[messages.messages.length - 1];

        _DATA_POINTER = last.id;

        if (messages.totalCount == 0){
            _LOGGER.info("Group: {} ({}) done.", groupTag, groupId);
            return;
        }

        for (TdApi.Message message : messages.messages) {
            if (message == null)
                continue;

            final DerivedData derivedData = strategy.wrap(message, DefaultExceptionHandler.INSTANCE);

            if (derivedData != null) {
                bot.getDataService().saveData(derivedData);

                _LOGGER.info("Message ({}:{}) accomplished.", groupId, message.id);
            }
        }

        try {
            Thread.sleep(_TELEGRAM_THRESHOLD * 1000);
        } catch (InterruptedException e) {
            _LOGGER.error("Can't await cool-down.", e);
        }

        client.send(
                new TdApi.GetChatHistory(groupId, _DATA_POINTER, 0, 50, false),
                (cRes) -> proceed(client, bot, groupTag, groupId, cRes)
        );
    }

}
