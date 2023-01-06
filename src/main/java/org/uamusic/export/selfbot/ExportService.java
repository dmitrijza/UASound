package org.uamusic.export.selfbot;

import java.util.concurrent.TimeUnit;

/**
 * A service that manages self-bot data to a storage.
 */

public interface ExportService {

    void init();

    void scheduleExport(final String groupTag, final long time, final TimeUnit unit);

    void export(final String groupTag);

}
