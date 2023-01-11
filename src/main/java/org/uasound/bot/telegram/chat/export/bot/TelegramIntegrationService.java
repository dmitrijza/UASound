package org.uasound.bot.telegram.chat.export.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uasound.bot.telegram.TelegramBot;
import org.uasound.data.entity.DerivedData;
import org.uasound.data.entity.SharedAudio;
import org.uasound.data.service.DataService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TelegramIntegrationService implements ExportIntegrationService {

    private final TelegramBot bot;

    private final DataService dataService;

    private final BotIntegrationStrategy strategy;

    public static final long BUCKET_CHAT_ID = -1001607879665L;

    private final IntegrationUpdatesHandler updatesHandler;

    private final Map<DerivedData, SharedAudio> cache = new HashMap<>();

    static final Logger _LOGGER = LoggerFactory.getLogger(TelegramIntegrationService.class);

    public TelegramIntegrationService(final TelegramBot bot){
        this.bot = bot;
        this.dataService = bot.getDataService();
        this.strategy = new BotIntegrationStrategy(this, bot.getTelegramClient());

        this.updatesHandler = new DefaultIntegrationHandler(this, this.strategy);
    }

    @Override
    public void init() {
        this.strategy.setBucketChat(BUCKET_CHAT_ID);
        this.bot.setUpdatesHandler(updatesHandler);

        this.bot.schedule(() -> {
            dataService.getCacheableData()
                    .forEach((id) -> {
                        final DerivedData data = this.dataService.getData(id);

                        if (!(this.dataService.containsAudio(data.getInternalId())))
                            integrate(data, (audio) -> {
                                _LOGGER.debug(
                                        "Integrated audio (id={}) as part of scheduled action", audio.getInternalId());
                            });
                    });
        }, 10, TimeUnit.SECONDS);
    }

    @Override
    public void integrate(DerivedData data, Consumer<SharedAudio> consumer) {
        if (updatesHandler.isAwaited(data))
            return;

        if (dataService.containsAudio(data.getInternalId())) {
            consumer.accept(dataService.getAudio(data.getInternalId()));
        } else {
            if (cache.containsKey(data)){
                consumer.accept(cache.get(data));
                return;
            }

            this.strategy.integrate(data, (sharedAudio) -> {
                cache.put(data, sharedAudio);
                consumer.accept(sharedAudio);
            });
        }
    }

    @Override
    public void forceCache(DerivedData data, SharedAudio audio) {
        this.cache.put(data, audio);
    }

    @Override
    public IntegrationUpdatesHandler getUpdatesHandler() {
        return updatesHandler;
    }

    @Override
    public DataService getDataService() {
        return dataService;
    }
}
