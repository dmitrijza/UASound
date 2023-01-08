package org.uamusic.bot.telegram.chat.export.bot;

import org.uamusic.data.service.PG.PGDataService;
import org.uamusic.bot.telegram.chat.export.bot.entity.SharedAudio;
import org.uamusic.data.entity.DerivedData;

import java.util.function.Consumer;

/**
 * A service that resolves {@link DerivedData} into {@link SharedAudio} using its own
 * strategy.
 */
public interface ExportIntegrationService {

    void init();

    void integrate(final DerivedData data, final Consumer<SharedAudio> consumer);

    void forceCache(final DerivedData data, final SharedAudio audio);

    IntegrationUpdatesHandler getUpdatesHandler();

    PGDataService getDataService();

}
