package org.uasound.bot.telegram.chat.export.selfbot;

import it.tdlight.client.Result;
import it.tdlight.jni.TdApi;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uasound.bot.telegram.TelegramBot;
import org.uasound.bot.telegram.chat.export.selfbot.strategy.ExportStrategy;
import org.uasound.data.entity.AlbumLinkage;
import org.uasound.data.entity.DerivedAlbum;
import org.uasound.data.entity.DerivedData;
import org.uasound.data.entity.GroupCard;
import org.uasound.data.service.DataService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The synchronous export service that provides a way
 * to save audio from certain (=pre-defined) channels.
 */

@RequiredArgsConstructor
public class SelfBotExportService implements ExportService {
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

    private final TelegramBot botInstance;

    private SelfBotAdapter telegramClient;
    private final ExportStrategy<TdApi.Message> strategy = new SelfBotExportStrategy();

    static final ScheduledExecutorService _EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    static long _DATA_POINTER = 0, _PROCESSED = 0;
    static final Logger _LOGGER = LoggerFactory.getLogger(SelfBotExportService.class);

    static final Pattern TAG_PATTERN = Pattern.compile("#\\w+");

    static final List<DerivedData> audioSequence = new ArrayList<>();

    @Override
    public void init() {
        telegramClient = botInstance.getTelegramClient();

        // We will be integrating new data once it's available.
        telegramClient.registerUpdateHandler(TdApi.UpdateNewMessage.class, (update) -> {
            final TdApi.Message message = update.message;

            if (message == null)
                return;

            final List<Long> registeredGroups = botInstance.getDataService().getGroupIdList();
            final DataService dataService = botInstance.getDataService();

            if (registeredGroups.contains(message.chatId)){
                final DerivedData data = this.strategy.wrap(message, DefaultExceptionHandler.INSTANCE);
                dataService.saveData(data);

                botInstance.getIntegrationService().integrate(data, (sharedAudio) -> {
                    _LOGGER.debug("Integrated new DerivedData (id={}) from update (groupId={}).",
                            data.getPostId(),
                            data.getGroupId()
                    );
                });
            }
        });

        botInstance.schedule(() -> {
            final DataService dataService = botInstance.getDataService();
            final Collection<Long> groups = dataService.getGroupIdList();

            for (Long id : groups){
                final GroupCard card = dataService.getGroupCard(id);

                this.export(card.getGroupTag(), 50);
            }

        }, 3, TimeUnit.HOURS);

        Runtime.getRuntime().addShutdownHook(new Thread(_EXECUTOR::shutdown));
    }

    @Override
    public void scheduleExport(String groupTag, long time, TimeUnit unit) {
        _LOGGER.info("Scheduled new export from {}", groupTag);

        _EXECUTOR.schedule(() -> this.export(groupTag, 0), time, unit);
    }

    public void export(final String groupTag, int limit) {
        _EXECUTOR.submit(() ->
                telegramClient.send(new TdApi.SearchChats(groupTag, 1), (rs) -> {
            final TdApi.Chats chats = rs.get();
            final long groupId = chats.chatIds[0];

            telegramClient.send(
                    new TdApi.GetChatHistory(groupId, _DATA_POINTER, 0, 1, false),
                    (res) -> proceed(telegramClient, botInstance, groupTag, groupId, limit, res)
            );
        }));
    }

    public void proceed(final SelfBotAdapter client,
                        final TelegramBot bot,
                        final String groupTag,
                        final long groupId,
                        final int limit,
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

                audioSequence.add(bot.getDataService().getData(derivedData.getGroupId(), derivedData.getPostId()));
                _PROCESSED++;
            } else {
                this.proceedAlbum(bot, message);
            }
        }

        try {
            Thread.sleep(_TELEGRAM_THRESHOLD * 1000);
        } catch (InterruptedException e) {
            _LOGGER.error("Can't await cool-down.", e);
        }

        if (_PROCESSED > limit) {
            _LOGGER.info("Limit exceeded for {}", groupId);
            return;
        }

        client.send(
                new TdApi.GetChatHistory(groupId, _DATA_POINTER, 0, 50, false),
                (cRes) -> proceed(client, bot, groupTag, groupId, limit, cRes)
        );
    }

    public void proceedAlbum(final TelegramBot bot, final TdApi.Message message){
        TdApi.MessageContent content = message.content;

        if (!(content instanceof TdApi.MessagePhoto)){
            _LOGGER.info("Message (group={}) {}: no caption content.", message.chatId, message.id);
            return;
        }

        final TdApi.MessagePhoto text = (TdApi.MessagePhoto) message.content;
        final String[] textContent = text.caption.text.split("\n");

        if (textContent.length < 3)
            return;

        final String label = textContent[0];

        if (!((label.contains(" - ") || label.contains(" — ")) && text.caption.text.contains("#")))
            return;

        String delimiter = "-";
        String[] labelSplit = label.split(delimiter);

        if (labelSplit.length != 2)
            delimiter = " — ";

        labelSplit = label.split(delimiter);

        if (labelSplit.length != 2)
            return;

        final String author = labelSplit[0],
                album = labelSplit[1];

        List<String> tagCompilation = new ArrayList<>();

        final Matcher tagMatcher = TAG_PATTERN.matcher(text.caption.text);

        while (tagMatcher.find()){
            tagCompilation.add(tagMatcher.group());
        }

        int year = 0;

        final Matcher matcher = Pattern.compile("\\(([^)]+)\\)").matcher(label);

        while (matcher.find()){
            final String yearS = matcher.group().replace("(", "").replace(")", "");

            // The most convenient verification of integer.
            if (!(yearS.startsWith("20") || yearS.startsWith("19")))
                continue;

            year = Integer.parseInt(yearS);
        }

        final DerivedAlbum albumObj = DerivedAlbum.builder()
                .name(album)
                .author(author)
                .tags(tagCompilation)
                .year(year)
                .build();

        bot.getDataService().saveAlbum(albumObj);

        final DerivedAlbum newAlbum = bot.getDataService().getAlbum(author, album, year);

        for (DerivedData data : audioSequence) {
            bot.getDataService().saveLinkage(AlbumLinkage.builder()
                    .albumId(newAlbum.getInternalId())
                    .postId(message.id)
                    .groupId(message.chatId)
                    .dataId(data.getInternalId())
                    .build());
        }

        audioSequence.clear();
    }
}
