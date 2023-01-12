package org.uasound.data.entity;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
@Entity
@Table(name = "post_meta")
@ToString
public class DerivedMeta implements Serializable {
    @Id
    @Column(name = "internal_id")
    @Setter
    private long internalId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "meta_data", columnDefinition = "hstore")
    @Setter
    private Map<String, String> meta = new HashMap<>();

    @Column(name = "schema")
    private String schema;

    @OneToOne
    private DerivedData data;

}
