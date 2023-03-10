package org.uasound.bot.telegram.chat.export.bot;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Audio;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.uasound.data.entity.DerivedData;
import org.uasound.data.entity.SharedAudio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class DefaultIntegrationHandler implements IntegrationUpdatesHandler {
    private final ExportIntegrationService integrationService;

    private final BotIntegrationStrategy strategy;

    private final Map<DerivedData, BiConsumer<DerivedData, SharedAudio>> query = new HashMap<>();

    static final Logger _LOGGER = LoggerFactory.getLogger(DefaultIntegrationHandler.class);

    @Override
    public void accept(List<Update> updates) {
        for (Update update : updates){
            if (!(update.hasChannelPost()))
                continue;

            final Message message = update.getChannelPost();

            if (!(message.hasAudio())) {
                _LOGGER.debug("{}: not an audio", message.getMessageId());
                continue;
            }

            if (message.getChatId() != TelegramIntegrationService.BUCKET_CHAT_ID) {
                _LOGGER.debug("{}: not a bucket chat.", message.getMessageId());
                continue;
            }

            final Audio audio = message.getAudio();

            _LOGGER.debug("{}: received shared audio", message.getMessageId());

            final List<DerivedData> processed = new ArrayList<>();

            for (Map.Entry<DerivedData, BiConsumer<DerivedData, SharedAudio>> entry : query.entrySet()) {
                final DerivedData data = entry.getKey();

                if (audio.getFileUniqueId().equals(data.getFileUniqueId())) {
                    final SharedAudio completeAudio = strategy.transform(data, message);

                    integrationService.getDataService().saveAudio(completeAudio);

                    this.query.get(data).accept(data, completeAudio);
                    processed.add(data);
                }
            }

            processed.forEach(this.query::remove);
        }
    }

    public void await(final DerivedData data, final BiConsumer<DerivedData, SharedAudio> consumer){
        this.query.put(data, consumer);
    }

    @Override
    public boolean isAwaited(DerivedData data) {
        return query.containsKey(data);
    }
}
