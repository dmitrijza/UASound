package org.uasound.data.service.hibernate.exact;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.uasound.data.entity.SharedAudio;
import org.uasound.data.service.PG.PGQuery;

@Repository
public interface AudioRepository extends CrudRepository<SharedAudio, Long> {

    SharedAudio getAudioByInternalId(long internalId);

    @Query(value = PGQuery.POSTGRE_SELECT_CACHEABLE_DATA, nativeQuery = true)
    Iterable<Long> cacheableData();

}
