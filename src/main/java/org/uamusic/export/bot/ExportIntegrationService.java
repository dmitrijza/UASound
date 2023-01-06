package org.uamusic.export.bot;

import org.uamusic.export.bot.bot.SharedAudio;
import org.uamusic.data.entity.DerivedData;

import java.util.function.Consumer;

/**
 * A service that resolves {@link DerivedData} into {@link SharedAudio} using its own
 * strategy.
 */
public interface ExportIntegrationService {

    void init();

    void integrate(final DerivedData data, final Consumer<SharedAudio> consumer);

}
