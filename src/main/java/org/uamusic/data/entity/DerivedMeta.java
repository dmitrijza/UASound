package org.uamusic.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@AllArgsConstructor
@Builder
public class DerivedMeta {
    @Setter
    private long internalId;

    @Setter
    private Map<String, String> meta;

    private final String schema;

}
