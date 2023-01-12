package org.uasound.data.service.hibernate.exact;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.uasound.data.entity.DerivedMeta;

@Repository
public interface DataMetaRepository extends CrudRepository<DerivedMeta, Long> {

    DerivedMeta getMetaByInternalId(long internalId);

}
