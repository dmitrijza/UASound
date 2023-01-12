package org.uasound.data.entity;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter @Getter
@Builder
@Table(name = "album")
@Entity
public class DerivedAlbum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    private long internalId;

    @Column(name = "author")
    private String author;

    @Column(name = "album_name")
    private String name;

    @Column(name = "year")
    private long year;

    @Type(ListArrayType.class)
    @Column(name = "tags", columnDefinition = "varchar[]")
    private List<String> tags = new ArrayList<>();

}
