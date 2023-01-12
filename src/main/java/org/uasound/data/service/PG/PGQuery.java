package org.uasound.data.service.PG;

public final class PGQuery {

    private PGQuery() { /* ... */ }


    static final String POSTGRE_SHARED_TABLE = "" +
            "create table if not exists shared_audio" +
            "(" +
            "    creation_timestamp     timestamp default CURRENT_TIMESTAMP not null," +
            "    modification_timestamp timestamp default CURRENT_TIMESTAMP not null," +
            "    internal_id            bigint                              not null," +
            "    post_id                bigint                              not null," +
            "    bucket_id              bigint                              not null," +
            "    unique_file_id              varchar                              not null," +
            "    remote_file_id              varchar                              not null," +
            "    file_id              varchar                              not null," +
            "    message_id             bigint                              not null" +
            "        constraint shared_audio_pk" +
            "            primary key," +
            "    schema                 varchar                             not null," +
            "    constraint shared_audio_pk2" +
            "        unique (internal_id, post_id, bucket_id, message_id)" +
            ");";

    static final String POSTGRE_SHARED_INSERT = "" +
            "" +
            "insert into shared_audio (internal_id, post_id, bucket_id, message_id, " +
            "unique_file_id, remote_file_id, file_id," +
            "                          \"schema\")" +
            "values (?, ?, ?, ?, ?, ?, ?, ?) on conflict (internal_id, post_id, bucket_id, message_id) do nothing;";

    static final String POSTGRE_SHARED_SELECT = "" +
            "select * from shared_audio where internal_id = ?";

    static final String POSTGRE_SHARED_EXISTS = "" +
            "select exists (select * from shared_audio where internal_id = ?);";

    static final String POSTGRE_CARD_TABLE = "" +
            "create table if not exists group_cards" +
            "(" +
            "    creation_timestamp     timestamp default CURRENT_TIMESTAMP not null," +
            "    modification_timestamp timestamp default CURRENT_TIMESTAMP not null," +
            "    group_prefix           varchar," +
            "    group_id               bigint                              not null" +
            "        constraint group_cards_pk" +
            "            primary key," +
            "    group_title            varchar                             not null," +
            "    group_tag            varchar                             not null," +
            "    group_invite_id            varchar                             not null," +
            "    administrator_id       bigint                              not null," +
            "    initiator              bigint                              not null," +
            "    schema                 varchar                             not null" +
            ");";

    static final String POSTGRE_DATA_TABLE = "" +
            "create table if not exists post_data" +
            "(" +
            "    creation_timestamp timestamp default CURRENT_TIMESTAMP," +
            "    modification_timestamp   timestamp default CURRENT_TIMESTAMP," +
            "    internal_id        bigserial" +
            "        constraint post_data_pk" +
            "            primary key," +
            "    group_id           bigint  not null," +
            "    post_id            bigint  not null," +
            "    file_id            varchar," +
            "    remote_file_id            varchar," +
            "    unique_file_id            varchar," +
            "    track_name         varchar not null," +
            "    track_duration     varchar not null," +
            "    track_performer    varchar," +
            "    aggregator          varchar not null," +
            "    \"schema\"             varchar not null," +
            "    constraint post_data_pk2" +
            "        unique (group_id, post_id, track_name, track_duration)" +
            ");";
    static final String POSTGRE_META_TABLE = "create table if not exists post_meta" +
            "(" +
            "    internal_id bigserial" +
            "        constraint post_meta_pk" +
            "            primary key" +
            "        constraint post_meta_pk2" +
            "            unique," +
            "    meta_data   hstore  not null," +
            "    schema      varchar not null" +
            ");";
    static final String POSTGRE_META_UPSERT = "" +
            "insert into post_meta (internal_id, meta_data, \"schema\")" +
            "values (?, ?, ?) on conflict (internal_id) do nothing;";
    static final String POSTGRE_META_UPDATE = "" +
            "UPDATE post_meta SET meta_data = ? where internal_id = ?;";

    static final String POSTGRE_DATA_UPDATE = "" +
            "update post_data set unique_file_id = ?, remote_file_id = ?, file_id = ?, track_name = ?, " +
            "track_duration = ?, track_performer = ?, aggregator = ?, " +
            "schema = ?, creation_timestamp = ?, modification_timestamp = ? " +
            "where internal_id = ?;";
    static final String POSTGRE_DATA_CONTAINS = "" +
            "select exists (select * from post_data where group_id = ? and post_id = ?);";
    static final String POSTGRE_DATA_SELECT_EXACT = "" +
            "select * from post_data where group_id = ? and post_id = ?;";

    static final String POSTGRE_DATA_SELECT_FILE = "" +
            "select * from post_data where file_id = ?";

    static final String POSTGRE_META_SELECT = "" +
            "select * from post_meta where internal_id = ?;";
    static final String POSTGRE_CARD_UPDATE = "" +
            "update group_cards set administrator_id = ? where group_id = ?;";
    static final String POSTGRE_CARD_SELECT = "" +
            "select * from group_cards where group_id = ?";
    static final String POSTGRE_CARD_SELECT_ALL_IDS = "" +
            "select group_id from group_cards;";
    static final String POSTGRES_SELECT_AUDIO_EXISTS = "select exists(select * from shared_audio where" +
            "group_id = ? and post_id = ? and track_name = ? and track_duration = ?);";
    static final String POSTGRE_CARD_SELECT_TAG = "" +
            "select * from group_cards where group_tag = ?;";

    static final String SAVE_ALBUM = "" +
            "insert into album(author, album_name, year, tags) values (?, ?, ?, ?) on conflict (author, album_name, year) do nothing;";

    static final String GET_ALBUM = "" +
            "select * from album where author = ? and album_name = ? and year = ?;";

    static final String SAVE_LINKAGE = "" +
            "insert into album_links (album_id, group_id, post_id, data_id) values (" +
            "?, ?, ?, ?) on conflict (album_id, data_id) do nothing;";
    public static final String GET_LINKED_FILES = "" +
            "select * from album_links where album_id = ?;";

    static final String GET_LINKAGE = "" +
            "select * from album_links where data_id = ?";

    static final String GET_ALBUM_ID = "" +
            "select * from album where internal_id = ?";

    public static final String POSTGRE_SELECT_CACHEABLE_DATA = "" +
            "select t1.internal_id from post_data t1 left join shared_audio t2 on " +
            "t2.internal_id = t1.internal_id where t2.internal_id is null limit 5;";

    static final String POSTGRE_DATA_SELECT_INTERNAL = "" +
            "select * from post_data where internal_id = ?;";

    static final String POSTGRE_MESSAGE_UPSERT = "" +
            "insert into post_data (group_id, post_id, file_id, " +
            "remote_file_id, unique_file_id, track_name, track_duration, track_performer," +
            "aggregator, \"schema\", creation_timestamp, modification_timestamp)" +
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "on conflict (group_id, post_id, track_name, track_duration) do nothing;";


    // :wheelchair:, needs to be replaced to a normal one indexation system.
    public static final String POSTGRE_SEARCH = "with reference(input_query) as (values('%s'))\n" +
            "select * from post_data, reference,\n" +
            "            to_tsvector(post_data.track_name || post_data.track_performer) document,  \n" +
            "            to_tsquery(input_query) query,\n" +
            "            nullif(ts_rank(to_tsvector(post_data.track_name || post_data.track_performer), query), 0) rank_title,  \n" +
            "            NULLIF(ts_rank(to_tsvector(post_data.track_name || post_data.track_performer), query), 0) rank_description,  \n" +
            "            SIMILARITY(input_query, post_data.track_name || post_data.track_performer) similarity\n" +
            "            WHERE query @@ document OR similarity > 0  \n" +
            "            ORDER BY rank_title, rank_description, similarity DESC NULLS LAST limit 10;";

    public static final String POSTGRE_SEARCH_ALBUM_AUTHOR = "" +
            "with reference(input_query) as (values('%s')) \n" +
            "            select * from album, reference, \n" +
            "                        to_tsvector(album.author) document,   \n" +
            "                        to_tsquery(input_query) query, \n" +
            "                        nullif(ts_rank(to_tsvector(album.author), query), 0) rank_title,   \n" +
            "                        NULLIF(ts_rank(to_tsvector(album.author), query), 0) rank_description,   \n" +
            "                        SIMILARITY(input_query, album.author) similarity \n" +
            "                        WHERE query @@ document OR similarity > 0   \n" +
            "                        ORDER BY rank_title, rank_description, similarity DESC NULLS LAST limit 10;";

    public static final String POSTGRE_SEARCH_ALBUM = "" +
            "with reference(input_query) as (values('%s')) \n" +
            "            select * from album, reference,\n" +
            "                        to_tsvector(album.author || album.album_name) document,\n" +
            "                        to_tsquery(input_query) query, \n" +
            "                        nullif(ts_rank(to_tsvector(album.author || album.album_name), query), 0) rank_title,\n" +
            "                        NULLIF(ts_rank(to_tsvector(album.author || album.album_name), query), 0) rank_description,\n" +
            "                        SIMILARITY(input_query, album.album_name || album.author) similarity\n" +
            "                        WHERE query @@ document OR similarity > 0   \n" +
            "                        ORDER BY rank_title, rank_description, similarity DESC NULLS LAST limit 10;";
}
