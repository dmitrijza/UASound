package org.uamusic.export.bot;

import it.tdlight.client.SimpleTelegramClient;
import org.uamusic.bot.telegram.TelegramBot;
import org.uamusic.data.entity.DerivedData;
import org.uamusic.export.bot.bot.SharedAudio;

import java.util.function.Consumer;

public class TelegramExportIntegrationService implements ExportIntegrationService {

    private final TelegramBot bot;

    private final BotIntegrationStrategy strategy;

    private final SimpleTelegramClient client;

    static final long BUCKET_CHAT_ID = 1001607879665L;

    public TelegramExportIntegrationService(final TelegramBot bot){
        this.bot = bot;
        this.client = bot.getTelegramClient();
        this.strategy = new BotIntegrationStrategy(client, bot);
    }

    @Override
    public void init() {
        this.strategy.setBucketChat(BUCKET_CHAT_ID);
    }

    @Override
    public void integrate(DerivedData data, Consumer<SharedAudio> consumer) {

    }
}
