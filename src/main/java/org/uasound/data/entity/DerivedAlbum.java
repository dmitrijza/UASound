package org.uasound.data.entity;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter @Getter
@Builder
public class DerivedAlbum {

    private long internalId;

    private String author, name;

    private long year;

    private List<String> tags;

}
