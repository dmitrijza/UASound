package org.uasound.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * The class whose emphasis is to provide ready-to-use
 * structures for a bot.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
@ToString
@Entity
@Table(name = "shared_audio")
public class SharedAudio implements Serializable {

    @Column(name = "creation_timestamp")
    @CreationTimestamp
    private Timestamp creationTimestamp;

    @Column(name = "modification_timestamp")
    @UpdateTimestamp
    private Timestamp modificationTimestamp;

    @Id
    @Column(name = "internal_id")
    private long internalId;

    @Column(name = "post_id", unique = true)
    private long postId;

    @Column(name = "bucket_id", unique = true)
    private long bucketId;

    @Column(name = "message_id", unique = true)
    private long messageId;

    @Column(name = "unique_file_id")
    private String fileUniqueId;

    @Column(name = "file_id")
    private String fileId;

    @Column(name = "remote_file_id")
    private String remoteFileId;

    @Column(name = "schema")
    private String schema;

}
