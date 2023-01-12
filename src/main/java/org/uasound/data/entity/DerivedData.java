package org.uasound.data.entity;

import it.tdlight.jni.TdApi;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.sql.Timestamp;


/**
 * Provides data for a plain {@link TdApi.Message} object within its audio structure.
 * Used to index through channels' messages.
 */
@ToString
@AllArgsConstructor
@Builder
@Getter @Setter
@Entity
@Table(name = "post_data",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"group_id", "post_id", "track_name", "track_duration"}
        )
)
@NoArgsConstructor
public class DerivedData implements Serializable {

    @Column(name = "creation_timestamp")
    @CreationTimestamp
    private Timestamp creationTimestamp;

    @Column(name = "modification_timestamp")
    @UpdateTimestamp
    private Timestamp modificationTimestamp;

    @OneToOne
    private DerivedMeta derivedMeta;

    @Id
    @Column(name = "internal_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long internalId;

    @Column(name = "group_id")
    private long groupId;

    @Column(name = "post_id")
    private long postId;

    @Column(name = "track_name")
    private String trackName;

    @Column(name = "track_duration")
    private String trackDuration;

    @Column(name = "track_performer")
    private String trackPerformer;

    @Column(name = "unique_file_id")
    private String fileUniqueId;

    @Column(name = "file_id")
    private String fileId;

    @Column(name = "remote_file_id")
    private String remoteFileId;

    @Column(name = "aggregator")
    private String aggregator;

    @Column(name = "schema")
    private String schema;
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DerivedData)
            return ((DerivedData) obj).internalId == this.getInternalId();
        return false;
    }
}
