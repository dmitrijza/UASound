package org.uamusic.bot.telegram.chat.export.bot;

import org.uamusic.bot.telegram.TelegramBot;
import org.uamusic.bot.telegram.chat.export.bot.entity.SharedAudio;
import org.uamusic.data.entity.DerivedData;
import org.uamusic.data.service.PG.PGDataService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TelegramIntegrationService implements ExportIntegrationService {

    private final TelegramBot bot;

    private final PGDataService dataService;

    private final BotIntegrationStrategy strategy;

    public static final long BUCKET_CHAT_ID = -1001607879665L;

    private final IntegrationUpdatesHandler updatesHandler;

    private final Map<DerivedData, SharedAudio> cache = new HashMap<>();

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
    }

    @Override
    public void integrate(DerivedData data, Consumer<SharedAudio> consumer) {
        if (updatesHandler.isAwaited(data))
            return;

        if (dataService.containsAudio(BigDecimal.valueOf(data.getInternalId()))) {
            consumer.accept(dataService.getAudio(BigDecimal.valueOf(data.getInternalId())));
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
    public PGDataService getDataService() {
        return dataService;
    }
}
