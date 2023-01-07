package org.uamusic.data.entity;

import lombok.*;

import java.sql.Timestamp;


/**
 * Provides data for a plain {@link it.tdlight.jni.TdApi.Message} object within its audio structure.
 * Used to index through channels' messages.
 */
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

    private final String fileUniqueId, fileId, remoteFileId;

    private final String aggregator;

    private final String schema;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DerivedData)
            return ((DerivedData) obj).internalId == this.getInternalId();
        return false;
    }
}
