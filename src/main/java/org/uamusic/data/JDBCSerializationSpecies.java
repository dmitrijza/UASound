package org.uamusic.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uamusic.data.entity.DerivedData;
import org.uamusic.data.entity.DerivedMeta;
import org.uamusic.data.service.PG.PGDataService;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Optional;

public final class JDBCSerializationSpecies {
    private JDBCSerializationSpecies() { /* ... */ }

    static final Logger _LOGGER = LoggerFactory.getLogger(JDBCSerializationSpecies.class);

    public static Optional<DerivedData> deserializeData(final PGDataService dataService, final ResultSet set){
        final DerivedData.DerivedDataBuilder builder = DerivedData.builder();
        try {
            final BigDecimal internalId = set.getBigDecimal("internal_id");

            builder.internalId(internalId.longValue());
            builder.groupId(set.getBigDecimal("group_id").longValue());
            builder.postId(set.getBigDecimal("post_id").longValue());
            builder.fileId(set.getString("file_id"));
            builder.trackName(set.getString("track_name"));
            builder.trackDuration(set.getString("track_duration"));
            builder.trackPerformer(set.getString("track_performer"));
            builder.derivedMeta(dataService.getMeta(internalId));
            builder.aggregator(set.getString("aggregator"));
            builder.schema(set.getString("schema"));
            builder.creationTimestamp(set.getTimestamp("creation_timestamp"));
            builder.modificationTimestamp(set.getTimestamp("modification_timestamp"));

            return Optional.of(builder.build());
        } catch (Exception e){
            _LOGGER.error("Can't deserialize message.", e);
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static Optional<DerivedMeta> deserializeMeta(final ResultSet set){
        try {
            final DerivedMeta.DerivedMetaBuilder builder = DerivedMeta.builder();

            while (set.next()){
                final BigDecimal internalId = set.getBigDecimal("internal_id");

                if (internalId == null)
                    return Optional.empty();

                builder.internalId(internalId.longValue());
                builder.meta((Map<String, String>) set.getObject("meta_data"));
                builder.schema(set.getString("schema"));
            }

            return Optional.of(builder.build());
        } catch (Exception e){
            _LOGGER.error("Can't deserialize meta.", e);
        }

        return Optional.empty();
    }

    public static PreparedStatement serializeData(final PreparedStatement statement, final DerivedData data){
        try {
            statement.setBigDecimal(1, BigDecimal.valueOf(data.getGroupId()));
            statement.setBigDecimal(2, BigDecimal.valueOf(data.getPostId()));
            statement.setString(3, data.getFileId());
            statement.setString(4, data.getTrackName());
            statement.setString(5, data.getTrackDuration());
            statement.setString(6, data.getTrackPerformer());
            statement.setString(7, data.getAggregator());
            statement.setString(8, data.getSchema());
            statement.setTimestamp(9, data.getCreationTimestamp());
            statement.setTimestamp(10, data.getModificationTimestamp());

            return statement;
        } catch (Exception e){
            _LOGGER.error("Can't serialize data", e);
            return statement;
        }
    }
}
