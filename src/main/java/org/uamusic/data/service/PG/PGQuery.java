package org.uamusic.data.service.PG;

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
            "values (?, ?, ?, ?, ?, ?, ?, ?);";

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
    static final String POSTGRE_DATA_SELECT = "" +
            "select * from post_data where group_id = ? and post_id = ?;";
    static final String POSTGRE_META_SELECT = "" +
            "select * from post_meta where internal_id = ?;";

    static final String POSTGRE_CARD_SELECT = "" +
            "select * from group_cards where group_id = ?";
    static final String POSTGRE_MESSAGE_UPSERT = "" +
            "insert into post_data (group_id, post_id, file_id, " +
            "remote_file_id, unique_file_id, track_name, track_duration, track_performer," +
            "aggregator, \"schema\", creation_timestamp, modification_timestamp)" +
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "on conflict (group_id, post_id, track_name, track_duration) do nothing;";

    // :wheelchair:, needs to be replaced to a normal one indexation system.
    static final String POSTGRE_SEARCH = "select " +
            "internal_id, group_id, post_id, unique_file_id, remote_file_id, file_id, track_name, track_duration, track_performer, aggregator, " +
            "\"schema\", creation_timestamp, modification_timestamp, similarity " +
            "from post_data, " +
            "to_tsvector(post_data.track_name || post_data.track_performer) document, " +
            "to_tsquery('%s') query, " +
            "nullif(ts_rank(to_tsvector(post_data.track_name || post_data.track_performer), query), 0) rank_title, " +
            "NULLIF(ts_rank(to_tsvector(post_data.track_name || post_data.track_performer), query), 0) rank_description, " +
            "SIMILARITY('%s', post_data.track_name || post_data.track_performer) similarity " +
            "WHERE query @@ document OR similarity > 0 " +
            "ORDER BY rank_title, rank_description, similarity DESC NULLS LAST limit 10;";
}
