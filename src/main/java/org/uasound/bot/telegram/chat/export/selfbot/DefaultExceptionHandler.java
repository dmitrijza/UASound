package org.uasound.bot.telegram.chat.export.selfbot;

import it.tdlight.jni.TdApi;

public class DefaultExceptionHandler implements SelfBotExportExceptionHandler {
    private DefaultExceptionHandler() { /* ... */ }

    static SelfBotExportExceptionHandler INSTANCE = new DefaultExceptionHandler();

    @Override
    public void accept(TdApi.Message message, Exception e) {
        SelfBotExportStrategy._LOGGER.error("Error occurred while exporting message (id={})", message.id, e);
    }
}
