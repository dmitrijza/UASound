package org.uamusic.data.entity;

import lombok.*;

import java.sql.Timestamp;


/**
 * Provides data for a plain {@link it.tdlight.jni.TdApi.Message} object within its audio structure.
 * Used to index through channels' messages.
 */

// Todo: provide lightweight version. (e.g. cache values)
@ToString
@AllArgsConstructor
@Builder @Getter
public class DerivedData {

    private final Timestamp creationTimestamp;

    private final Timestamp modificationTimestamp;

    @Setter
    private DerivedMeta derivedMeta;

    @Setter
    private long internalId;

    private final long groupId;

    private final long postId;

    private final String trackName;

    private final String trackDuration;

    private final String trackPerformer;

    private final String fileId;

    private final String aggregator;

    private final String schema;

}
