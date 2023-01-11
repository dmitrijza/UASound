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

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
@Entity
@Table(name = "group_cards")
public class GroupCard implements Serializable {

    @Column(name = "creation_timestamp")
    @CreationTimestamp
    private Timestamp creationTimestamp;

    @Column(name = "modification_timestamp")
    @UpdateTimestamp
    private Timestamp modificationTimestamp;

    @Column(name = "group_prefix")
    private String groupPrefix;

    @Column(name = "group_title")
    private String groupTitle;

    @Column(name = "group_tag")
    private String groupTag;

    @Column(name = "group_invite_id")
    private String groupInviteId;

    @Id
    @Column(name = "group_id")
    private long groupId;

    @Column(name = "administrator_id")
    @Setter
    private long administratorId;

    @Column(name = "initiator")
    private String initiator;

    @Column(name = "schema")
    private String schema;

}
