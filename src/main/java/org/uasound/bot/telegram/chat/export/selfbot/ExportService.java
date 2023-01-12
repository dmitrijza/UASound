package org.uasound.bot.telegram.chat.export.selfbot;

import org.uasound.bot.telegram.chat.export.bot.ExportIntegrationService;

import java.util.concurrent.TimeUnit;

/**
 * A service that manages self-bot data to a storage.
 */

public interface ExportService {

    void init();

    void scheduleExport(final String groupTag, final long time, final TimeUnit unit);

    void export(final String groupTag, final int limit);

    ExportIntegrationService getIntegrationService();

}
