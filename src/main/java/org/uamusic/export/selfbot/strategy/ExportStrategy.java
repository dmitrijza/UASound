package org.uamusic.export.selfbot.strategy;

import org.uamusic.data.entity.DerivedData;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface ExportStrategy<T> {

    void transform(final T input, Consumer<DerivedData> consumer, final BiConsumer<T, Exception> exceptionHandler);

    DerivedData wrap(final T rawInput, final BiConsumer<T, Exception> exceptionHandler);

}
