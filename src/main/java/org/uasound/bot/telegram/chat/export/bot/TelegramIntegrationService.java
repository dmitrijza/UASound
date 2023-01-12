package org.uasound.bot.telegram.chat.export.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.uasound.bot.telegram.chat.export.selfbot.SelfBotAdapter;
import org.uasound.data.entity.DerivedData;
import org.uasound.data.entity.SharedAudio;
import org.uasound.data.service.DataService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
public class TelegramIntegrationService implements ExportIntegrationService {

    private final DataService dataService;

    private final BotIntegrationStrategy strategy;

    private IntegrationUpdatesHandler updatesHandler;

    private final Map<DerivedData, SharedAudio> cache = new HashMap<>();

    public static final long BUCKET_CHAT_ID = -1001607879665L;

    static final Logger _LOGGER = LoggerFactory.getLogger(TelegramIntegrationService.class);

    static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    public TelegramIntegrationService(final DataService dataService,
                                      final SelfBotAdapter adapter){
        this.dataService = dataService;

        this.strategy = new BotIntegrationStrategy(this, adapter);
    }

    @Override @Async
    public void init() {
        this.strategy.setBucketChat(BUCKET_CHAT_ID);

        executor.scheduleAtFixedRate(() -> {
            dataService.getCacheableData()
                    .forEach((id) -> {
                        final DerivedData data = this.dataService.getData(id);

                        if (!(this.dataService.containsAudio(data.getInternalId())))
                            integrate(data, (audio) -> {
                                _LOGGER.debug(
                                        "Integrated audio (id={}) as part of scheduled action", audio.getInternalId());
                            });
                    });
        }, 5, 5, TimeUnit.SECONDS);
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
    @Bean @Lazy
    public IntegrationUpdatesHandler getUpdatesHandler() {
        this.updatesHandler = new DefaultIntegrationHandler(this, this.strategy);

        return updatesHandler;
    }

    @Override
    public DataService getDataService() {
        return dataService;
    }
}
