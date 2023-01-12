package org.uasound.data.service.hibernate.exact;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.uasound.data.entity.AlbumLinkage;

import java.util.Optional;

@Repository
public interface LinkageRepository extends CrudRepository<AlbumLinkage, Long> {

    Iterable<AlbumLinkage> findAllByAlbumId(long albumId);

    Optional<AlbumLinkage> findAlbumLinkageByDataId(long internalId);

}
