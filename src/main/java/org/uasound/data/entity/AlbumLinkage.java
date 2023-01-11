package org.uasound.data.entity;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class AlbumLinkage {

    private long albumId;

    private long groupId;

    private long postId;

    private long dataId;

}
