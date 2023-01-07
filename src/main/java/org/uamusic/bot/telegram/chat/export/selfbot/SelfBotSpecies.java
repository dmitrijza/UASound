package org.uamusic.bot.telegram.chat.export.selfbot;

import it.tdlight.client.APIToken;
import it.tdlight.client.AuthenticationData;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.client.TDLibSettings;
import it.tdlight.common.Init;
import it.tdlight.jni.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public final class SelfBotSpecies {
    private SelfBotSpecies(){ /* ... */ }

    static final Logger _LOGGER = LoggerFactory.getLogger(SelfBotSpecies.class);

    static SimpleTelegramClient _CLIENT = null;

    public static SimpleTelegramClient authorize() throws InterruptedException {
        if (_CLIENT != null) {
            return _CLIENT;
        }

        try {
            Init.start();
        } catch (Exception e) {
            _LOGGER.error("Can't init", e);
        }

        final APIToken apiToken = APIToken.example();
        final TDLibSettings settings = TDLibSettings.create(apiToken);

        final Path sessionPath = Paths.get("telegram");
        settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
        settings.setDownloadedFilesDirectoryPath(sessionPath.resolve("downloads"));

        _CLIENT = new SimpleTelegramClient(settings);

        final AuthenticationData data = AuthenticationData.consoleLogin();

        _CLIENT.execute(new TdApi.SetLogVerbosityLevel(0));
        _CLIENT.addUpdateHandler(TdApi.UpdateNewMessage.class, (updateNewMessage) ->
                System.out.println(updateNewMessage.message.content));

        _CLIENT.start(data);

        Thread.sleep(5000);

        return _CLIENT;
    }

    public static void authorize(
            final Consumer<SimpleTelegramClient> consumer,
            final Consumer<Exception> exceptionHandler)
    {
        try {
            consumer.accept(authorize());
        } catch (InterruptedException e) {
            exceptionHandler.accept(e);
        }
    }

}
