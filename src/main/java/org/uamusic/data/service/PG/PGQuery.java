package org.uamusic.data.service.PG;

public final class PGQuery {
    private PGQuery() { /* ... */ }
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
            "update post_data set file_id = ?, track_name = ?, " +
            "track_duration = ?, track_performer = ?, aggregator = ?, " +
            "schema = ?, creation_timestamp = ?, modification_timestamp = ? " +
            "where internal_id = ?;";
    static final String POSTGRE_DATA_CONTAINS = "" +
            "select exists (select * from post_data where group_id = ? and post_id = ?);";
    static final String POSTGRE_DATA_SELECT = "" +
            "select * from post_data where group_id = ? and post_id = ?;";
    static final String POSTGRE_META_SELECT = "" +
            "select * from post_meta where internal_id = ?;";
    static final String POSTGRE_MESSAGE_UPSERT = "" +
            "insert into post_data (group_id, post_id, file_id, track_name, track_duration, track_performer," +
            "aggregator, \"schema\", creation_timestamp, modification_timestamp)" +
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "on conflict (group_id, post_id, track_name, track_duration) do nothing;";
    // :wheelchair:, needs to be replaced to a normal one indexation system.

    static final String POSTGRE_SEARCH = "select " +
            "internal_id, group_id, post_id, file_id, track_name, track_duration, track_performer, aggregator, " +
            "\"schema\", creation_timestamp, modification_timestamp, similarity " +
            "from post_data, " +
            "to_tsvector(post_data.track_name || post_data.track_performer) document, " +
            "to_tsquery('%s') query, " +
            "nullif(ts_rank(to_tsvector(post_data.track_name), query), 0) rank_title, " +
            "NULLIF(ts_rank(to_tsvector(post_data.track_name), query), 0) rank_description, " +
            "SIMILARITY('%s', post_data.track_name) similarity " +
            "WHERE query @@ document OR similarity > 0 " +
            "ORDER BY rank_title, rank_description, similarity DESC NULLS LAST limit 10;";
}
