package org.uamusic.bot.telegram.chat.export.bot.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;

/**
 * The class whose emphasis is to provide ready-to-use
 * structures for a bot.
 */
@RequiredArgsConstructor
@Getter
@Builder
@ToString
public class SharedAudio {

    private final Timestamp creationTimestamp, modificationTimestamp;

    private final long internalId, postId, bucketId;

    private final long messageId;

    private final String fileUniqueId, fileId, remoteFileId;

    private final String schema;

}
