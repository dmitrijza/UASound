package org.uamusic.export.selfbot;

import it.tdlight.jni.TdApi;

import java.util.function.BiConsumer;

public interface SelfBotExportExceptionHandler extends BiConsumer<TdApi.Message, Exception> {

    @Override
    void accept(TdApi.Message message, Exception e);

}
