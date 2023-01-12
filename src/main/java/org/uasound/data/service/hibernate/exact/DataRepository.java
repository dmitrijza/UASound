package org.uasound.data.service.hibernate.exact;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.uasound.data.entity.DerivedData;

import java.util.Collection;

@Repository
public interface DataRepository extends CrudRepository<DerivedData, Long> {

    String POSTGRE_SEARCH = "with reference(input_query) as (values(:query))\n" +
            "select * from post_data, reference,\n" +
            "            to_tsvector(post_data.track_name || post_data.track_performer) document,  \n" +
            "            to_tsquery(input_query) query,\n" +
            "            nullif(ts_rank(to_tsvector(post_data.track_name || post_data.track_performer), query), 0) rank_title,  \n" +
            "            NULLIF(ts_rank(to_tsvector(post_data.track_name || post_data.track_performer), query), 0) rank_description,  \n" +
            "            SIMILARITY(input_query, post_data.track_name || post_data.track_performer) similarity\n" +
            "            WHERE query @@ document OR similarity > 0  \n" +
            "            ORDER BY rank_title, rank_description, similarity DESC NULLS LAST limit 10;";
    
    boolean existsByGroupIdAndPostIdAndTrackNameAndTrackDuration(
            long groupId, long postId, String trackName, String trackDuration);

    DerivedData getDataByGroupIdAndPostId(long groupId, long postId);

    DerivedData getDataByInternalId(long internalId);

    DerivedData getDataByFileId(String fileId);

    DerivedData getDataByFileUniqueId(String fileId);

    boolean existsByGroupIdAndPostId(long groupId, long postId);

    @Query(value = POSTGRE_SEARCH, nativeQuery = true)
    Collection<DerivedData> search(String query);

}
