package org.uasound.bot.telegram.chat.export.bot;

import org.uasound.data.service.DataService;
import org.uasound.data.entity.SharedAudio;
import org.uasound.data.entity.DerivedData;

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

    DataService getDataService();

}
