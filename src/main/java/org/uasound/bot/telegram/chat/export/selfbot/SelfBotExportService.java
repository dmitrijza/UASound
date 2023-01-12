package org.uasound.bot.telegram.chat.export.selfbot;

import it.tdlight.client.Result;
import it.tdlight.jni.TdApi;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.uasound.bot.telegram.TelegramBot;
import org.uasound.bot.telegram.chat.export.bot.ExportIntegrationService;
import org.uasound.bot.telegram.chat.export.selfbot.strategy.ExportStrategy;
import org.uasound.data.entity.AlbumLinkage;
import org.uasound.data.entity.DerivedAlbum;
import org.uasound.data.entity.DerivedData;
import org.uasound.data.entity.GroupCard;
import org.uasound.data.service.DataService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The synchronous export service that provides a way
 * to save audio from certain (=pre-defined) channels.
 */

@Service
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

    private final DataService dataService;

    @Getter
    private final ExportIntegrationService integrationService;

    private final TelegramBot instance;

    private final SelfBotAdapter botAdapter;

    private final ExportStrategy<TdApi.Message> strategy = new SelfBotExportStrategy();

    static long _DATA_POINTER = 0, _PROCESSED = 0;
    static final List<DerivedData> audioSequence = new ArrayList<>();

    static final Logger _LOGGER = LoggerFactory.getLogger(SelfBotExportService.class);
    static final Pattern TAG_PATTERN = Pattern.compile("#\\w+");
    static final int _TELEGRAM_THRESHOLD = 5;

    @Autowired
    public SelfBotExportService(final TelegramBot bot,
                                final DataService dataService,
                                final ExportIntegrationService integrationService,
                                final SelfBotAdapter selfBotAdapter){
        this.instance = bot;
        this.dataService = dataService;
        this.integrationService = integrationService;
        this.botAdapter = selfBotAdapter;
    }

    @Override @Async
    public void init() {
        // We will be integrating new data once it's available.
        botAdapter.registerUpdateHandler(TdApi.UpdateNewMessage.class, (update) -> {
            final TdApi.Message message = update.message;

            if (message == null)
                return;

            final List<Long> registeredGroups = dataService.getGroupIdList();

            if (registeredGroups.contains(message.chatId)){
                final DerivedData data = this.strategy.wrap(message, DefaultExceptionHandler.INSTANCE);

                dataService.saveData(data);

                integrationService.integrate(data, (sharedAudio) -> {
                    _LOGGER.debug("Integrated new DerivedData (id={}) from update (groupId={}).",
                            data.getPostId(),
                            data.getGroupId()
                    );
                });
            }
        });

        instance.schedule(() -> {
            final Collection<Long> groups = dataService.getGroupIdList();

            for (Long id : groups){
                final GroupCard card = dataService.getGroupCard(id);

                this.export(card.getGroupTag(), 50);
            }
        }, 3, TimeUnit.HOURS);
    }

    @Override
    public void scheduleExport(String groupTag, long time, TimeUnit unit) {
        _LOGGER.info("Scheduled new export from {}", groupTag);

        instance.schedule(() -> this.export(groupTag, 0), time, unit);
    }

    public void export(final String groupTag, int limit) {
        instance.submit(() ->
                botAdapter.send(new TdApi.SearchChats(groupTag, 1), (rs) -> {
            final TdApi.Chats chats = rs.get();
            final long groupId = chats.chatIds[0];

            botAdapter.send(
                    new TdApi.GetChatHistory(groupId, _DATA_POINTER, 0, 1, false),
                    (res) -> proceed(botAdapter, groupTag, groupId, limit, res)
            );
        }));
    }

    public void proceed(final SelfBotAdapter client,
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
                dataService.saveData(derivedData);

                _LOGGER.info("Message ({}:{}) accomplished.", groupId, message.id);

                audioSequence.add(dataService.getData(derivedData.getGroupId(), derivedData.getPostId()));
                _PROCESSED++;
            } else {
                this.proceedAlbum(message);
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
                (cRes) -> proceed(client, groupTag, groupId, limit, cRes)
        );
    }

    public void proceedAlbum(final TdApi.Message message){
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
            final String yearStr = matcher.group().replace("(", "").replace(")", "");

            // The most convenient verification of integer.
            if (!(yearStr.startsWith("20") || yearStr.startsWith("19")))
                continue;

            year = Integer.parseInt(yearStr);
        }

        final DerivedAlbum albumObj = DerivedAlbum.builder()
                .name(album)
                .author(author)
                .tags(tagCompilation)
                .year(year)
                .build();

        dataService.saveAlbum(albumObj);

        final DerivedAlbum newAlbum = dataService.getAlbum(author, album, year);

        for (DerivedData data : audioSequence) {
            dataService.saveLinkage(AlbumLinkage.builder()
                    .albumId(newAlbum.getInternalId())
                    .postId(message.id)
                    .groupId(message.chatId)
                    .dataId(data.getInternalId())
                    .build());
        }

        audioSequence.clear();
    }
}
