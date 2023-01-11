package org.uasound.bot.telegram.chat.export.selfbot;

import it.tdlight.client.GenericResultHandler;
import it.tdlight.client.GenericUpdateHandler;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
public class SelfBotAdapter {
    private final SimpleTelegramClient client;

    static final int THRESHOLD = 2;

    static final ReentrantLock LOCK = new ReentrantLock();

    public <T extends TdApi.Update> void registerUpdateHandler(
            final Class<T> tClass, final GenericUpdateHandler<T> handler){
        client.addUpdateHandler(tClass, handler);
    }

    public <T extends TdApi.Object> void send(
            final TdApi.Function<T> function,
            final GenericResultHandler<T> handler) {
        this.client.send(function, handler);
    }

}
