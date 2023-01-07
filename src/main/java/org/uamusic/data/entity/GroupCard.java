package org.uamusic.data.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;

@RequiredArgsConstructor
@Getter
@Builder
public class GroupCard {

    private final Timestamp creationTimestamp, modificationTimestamp;

    private final String groupPrefix;

    private final String groupTitle;

    private final String groupTag;

    private final long groupId;

    private final long administratorId;

    private final String initiator;

    private final String schema;

}
