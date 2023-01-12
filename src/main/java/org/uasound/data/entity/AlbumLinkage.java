package org.uasound.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
@Entity
@Table(name = "album_links")
public class AlbumLinkage {

    @Id
    @Column(name = "album_id")
    private long albumId;

    @Column(name = "data_id")
    private long dataId;

    @Column(name = "group_id")
    private long groupId;

    @Column(name = "post_id")
    private long postId;


}
