package org.uamusic.export.bot;

import it.tdlight.client.SimpleTelegramClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.uamusic.bot.telegram.TelegramBot;
import org.uamusic.data.entity.DerivedData;
import org.uamusic.export.bot.bot.SharedAudio;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class BotIntegrationStrategy {
    private final SimpleTelegramClient client;

    private final TelegramBot bot;

    @Setter @Getter
    private long bucketChat;

    public void integrate(final DerivedData data, final Consumer<SharedAudio> consumer){

    }
}
