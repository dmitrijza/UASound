package org.uasound.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uasound.data.entity.*;
import org.uasound.data.service.DataService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public final class JDBCSerializationSpecies {
    private JDBCSerializationSpecies() { /* ... */ }

    static final Logger _LOGGER = LoggerFactory.getLogger(JDBCSerializationSpecies.class);

    public static Optional<GroupCard> deserializeCard(final DataService dataService, final ResultSet set){
        final GroupCard.GroupCardBuilder builder = GroupCard.builder();

        try {
            builder.creationTimestamp(set.getTimestamp("creation_timestamp"));
            builder.modificationTimestamp(set.getTimestamp("modification_timestamp"));
            builder.groupPrefix(set.getString("group_prefix"));
            builder.groupId(set.getLong("group_id"));
            builder.groupTitle(set.getString("group_title"));
            builder.groupTag(set.getString("group_tag"));
            builder.groupInviteId(set.getString("group_invite_id"));
            builder.administratorId(set.getLong("administrator_id"));
            builder.initiator(set.getString("initiator"));
            builder.schema(set.getString("schema"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.of(builder.build());
    }

    public static Optional<DerivedAlbum> deserializeAlbum(final DataService service, final ResultSet set){
        final DerivedAlbum.DerivedAlbumBuilder builder = DerivedAlbum.builder();

        try {
            builder.name(set.getString("album_name"));
            builder.internalId(set.getLong("internal_id"));
            builder.author(set.getString("author"));
            builder.year(set.getLong("year"));
        } catch (Exception e){
            e.printStackTrace();
        }

        return Optional.ofNullable(builder.build());
    }

    public static Optional<SharedAudio> deserializeAudio(final DataService dataService, final ResultSet set){
        final SharedAudio.SharedAudioBuilder builder = SharedAudio.builder();

        try {
            builder.creationTimestamp(set.getTimestamp("creation_timestamp"));
            builder.modificationTimestamp(set.getTimestamp("modification_timestamp"));
            builder.internalId(set.getLong("internal_id"));
            builder.postId(set.getLong("post_id"));
            builder.bucketId(set.getLong("bucket_id"));
            builder.fileId(set.getString("file_id"));
            builder.fileUniqueId(set.getString("unique_file_id"));
            builder.remoteFileId(set.getString("remote_file_id"));
            builder.messageId(set.getLong("message_id"));
            builder.schema(set.getString("schema"));

            return Optional.of(builder.build());
        } catch (Exception e){
            _LOGGER.error("Can't deserialize audio.", e);
            return Optional.empty();
        }
    }

    public static Optional<DerivedData> deserializeData(final DataService dataService, final ResultSet set){
        final DerivedData.DerivedDataBuilder builder = DerivedData.builder();
        try {
                final long internalId = set.getLong("internal_id");

            builder.internalId(internalId);
            builder.groupId(set.getLong("group_id"));
            builder.postId(set.getLong("post_id"));
            builder.fileId(set.getString("file_id"));
            builder.fileUniqueId(set.getString("unique_file_id"));
            builder.remoteFileId(set.getString("remote_file_id"));
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
                final long internalId = set.getLong("internal_id");

                builder.internalId(internalId);
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
            statement.setLong(1, data.getGroupId());
            statement.setLong(2, data.getPostId());
            statement.setString(3, data.getFileId());
            statement.setString(4, data.getRemoteFileId());
            statement.setString(5, data.getFileUniqueId());
            statement.setString(6, data.getTrackName());
            statement.setString(7, data.getTrackDuration());
            statement.setString(8, data.getTrackPerformer());
            statement.setString(9, data.getAggregator());
            statement.setString(10, data.getSchema());
            statement.setTimestamp(11, data.getCreationTimestamp());
            statement.setTimestamp(12, data.getModificationTimestamp());

            return statement;
        } catch (Exception e){
            _LOGGER.error("Can't serialize data", e);
            return statement;
        }
    }
}
