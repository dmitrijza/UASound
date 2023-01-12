package org.uasound.data.service.hibernate.exact;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.uasound.data.entity.GroupCard;

@Repository
public interface CardRepository extends CrudRepository<GroupCard, Long> {

    GroupCard getGroupCardByGroupId(long groupId);

    GroupCard getGroupCardByGroupTag(String groupTag);

    @Query(value = "select p.group_id from group_cards p", nativeQuery = true)
    Iterable<Long> findAllIds();

}
