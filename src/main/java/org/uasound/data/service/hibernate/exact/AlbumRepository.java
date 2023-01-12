package org.uasound.data.service.hibernate.exact;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.uasound.data.entity.DerivedAlbum;

import java.util.Optional;

@Repository
public interface AlbumRepository extends CrudRepository<DerivedAlbum, Long> {

    String POSTGRE_SEARCH_ALBUM_AUTHOR = "" +
            "with reference(input_query) as (values(:query)) \n" +
            "            select * from album, reference, \n" +
            "                        to_tsvector(album.author) document,   \n" +
            "                        to_tsquery(input_query) query, \n" +
            "                        nullif(ts_rank(to_tsvector(album.author), query), 0) rank_title,   \n" +
            "                        NULLIF(ts_rank(to_tsvector(album.author), query), 0) rank_description,   \n" +
            "                        SIMILARITY(input_query, album.author) similarity \n" +
            "                        WHERE query @@ document OR similarity > 0   \n" +
            "                        ORDER BY rank_title, rank_description, similarity DESC NULLS LAST limit 10;";

    String POSTGRE_SEARCH_ALBUM = "with reference(input_query) as (values(:query)) \n" +
            "            select * from album, reference,\n" +
            "                        to_tsvector(album.author || album.album_name) document,\n" +
            "                        to_tsquery(input_query) query, \n" +
            "                        nullif(ts_rank(to_tsvector(album.author || album.album_name), query), 0) rank_title,\n" +
            "                        NULLIF(ts_rank(to_tsvector(album.author || album.album_name), query), 0) rank_description,\n" +
            "                        SIMILARITY(input_query, album.album_name || album.author) similarity\n" +
            "                        WHERE query @@ document OR similarity > 0   \n" +
            "                        ORDER BY rank_title, rank_description, similarity DESC NULLS LAST limit 10;";

    @Query(value = POSTGRE_SEARCH_ALBUM, nativeQuery = true)
    Iterable<DerivedAlbum> searchAlbum(String query);

    @Query(value = POSTGRE_SEARCH_ALBUM_AUTHOR, nativeQuery = true)
    Iterable<DerivedAlbum> searchAlbumAuthor(String query);

    Optional<DerivedAlbum> findDerivedAlbumByAuthorAndNameAndYear(String author, String name, long year);

}
