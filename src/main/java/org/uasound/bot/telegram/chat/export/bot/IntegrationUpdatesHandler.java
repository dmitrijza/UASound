package org.uasound.bot.telegram.chat.export.bot;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.uasound.data.entity.DerivedData;
import org.uasound.data.entity.SharedAudio;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IntegrationUpdatesHandler extends Consumer<List<Update>> {

    void await(final DerivedData data, final BiConsumer<DerivedData, SharedAudio> consumer);

    boolean isAwaited(final DerivedData data);

}
