package org.uasound.data.service.PG;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uasound.data.JDBCSerializationSpecies;
import org.uasound.data.entity.DerivedData;
import org.uasound.data.entity.DerivedMeta;
import org.uasound.data.entity.GroupCard;
import org.uasound.data.entity.SharedAudio;
import org.uasound.data.service.DataService;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of DataService that stores data in PostgresSQL.
 * Hardcode at its best, needs to be replaced to canonical type of storage possession.
 */
public final class PGDataService implements DataService {
    @Getter
    private HikariDataSource dataSource;

    private PreparedStatement
            dataUpsert, dataUpdate, dataContains, dataByInternal,
            sharedInsert, sharedSelect, sharedExists, sharedCacheable,sharedExists1,
            metaUpdate, metaUpsert, metaSelect, dataSelect,
            groupCardSelect, groupCardIdsSelect, saveGroupCard, groupCardSelectTag;

    private Statement search;

    static final Logger LOGGER = LoggerFactory.getLogger(PGDataService.class);

    @Override
    public void init(){
        final HikariConfig config = new HikariConfig("configuration/data.properties");
        this.dataSource = new HikariDataSource(config);

        try {
            final Connection connection = this.dataSource.getConnection();
            final Statement prepareStatement = connection.createStatement();

            prepareStatement.executeUpdate(PGQuery.POSTGRE_DATA_TABLE);
            prepareStatement.executeUpdate(PGQuery.POSTGRE_META_TABLE);
            prepareStatement.executeUpdate(PGQuery.POSTGRE_SHARED_TABLE);
            prepareStatement.executeUpdate(PGQuery.POSTGRE_CARD_TABLE);

            this.search = connection.createStatement();

            this.groupCardSelect = connection.prepareStatement(PGQuery.POSTGRE_CARD_SELECT);
            this.groupCardIdsSelect = connection.prepareStatement(PGQuery.POSTGRE_CARD_SELECT_ALL_IDS);
            this.groupCardSelectTag = connection.prepareStatement(PGQuery.POSTGRE_CARD_SELECT_TAG);
            this.saveGroupCard = connection.prepareStatement(PGQuery.POSTGRE_CARD_UPDATE);

            this.dataUpsert = connection.prepareStatement(PGQuery.POSTGRE_MESSAGE_UPSERT, Statement.RETURN_GENERATED_KEYS);
            this.dataContains = connection.prepareStatement(PGQuery.POSTGRE_DATA_CONTAINS);
            this.dataSelect = connection.prepareStatement(PGQuery.POSTGRE_DATA_SELECT_EXACT);
            this.dataUpdate = connection.prepareStatement(PGQuery.POSTGRE_DATA_UPDATE);
            this.dataByInternal = connection.prepareStatement(PGQuery.POSTGRE_DATA_SELECT_INTERNAL);

            this.sharedSelect = connection.prepareStatement(PGQuery.POSTGRE_SHARED_SELECT);
            this.sharedExists = connection.prepareStatement(PGQuery.POSTGRE_SHARED_EXISTS);
            this.sharedInsert = connection.prepareStatement(PGQuery.POSTGRE_SHARED_INSERT);
            this.sharedCacheable = connection.prepareStatement(PGQuery.POSTGRE_SELECT_CACHEABLE_DATA);
            this.sharedExists1 = connection.prepareStatement(PGQuery.POSTGRES_SELECT_AUDIO_EXISTS);

            this.metaUpdate = connection.prepareStatement(PGQuery.POSTGRE_META_UPDATE);
            this.metaUpsert = connection.prepareStatement(PGQuery.POSTGRE_META_UPSERT);
            this.metaSelect = connection.prepareStatement(PGQuery.POSTGRE_META_SELECT, Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveGroupCard(final GroupCard card){
        try {
            this.saveGroupCard.setLong(1, card.getAdministratorId());
            this.saveGroupCard.setLong(2, card.getGroupId());

            this.saveGroupCard.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveData(final DerivedData derivedData){
        if (containsData(derivedData.getGroupId(), derivedData.getPostId())) {
            updateData(derivedData);
            return;
        }

        try {
            JDBCSerializationSpecies.serializeData(this.dataUpsert, derivedData);
            this.dataUpsert.executeUpdate();

            try(final ResultSet keys = this.dataUpsert.getGeneratedKeys()) {
                if (keys.next()) {
                    final BigDecimal internalId = keys.getBigDecimal(3);

                    if (derivedData.getDerivedMeta() != null)
                        derivedData.getDerivedMeta().setInternalId(internalId.longValue());
                    derivedData.setInternalId(internalId.longValue());
                }
            }

            if (derivedData.getDerivedMeta() != null)
                saveMeta(derivedData.getDerivedMeta());
        } catch (SQLException e) {
            LOGGER.error("Can't upsert message", e);
        }
    }

    @Override
    public void saveAudio(final SharedAudio audio){
        try {
            this.sharedInsert.setLong(1,audio.getInternalId());
            this.sharedInsert.setLong(2,audio.getPostId());
            this.sharedInsert.setLong(3,audio.getBucketId());
            this.sharedInsert.setLong(4,audio.getMessageId());
            this.sharedInsert.setString(5, audio.getFileUniqueId());
            this.sharedInsert.setString(6, audio.getRemoteFileId());
            this.sharedInsert.setString(7, audio.getFileId());
            this.sharedInsert.setString(8, audio.getSchema());

            this.sharedInsert.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean containsAudio(final long internalId){
        try {
            this.sharedExists.setLong(1, internalId);

            final ResultSet set = this.sharedExists.executeQuery();

            if (set.next())
                return set.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    @Override
    public List<Long> getGroupIdList(){
        try {
            final ResultSet set = this.groupCardIdsSelect.executeQuery();
            final List<Long> ids = new ArrayList<>();

            while (set.next()) {
                ids.add(set.getBigDecimal(1).longValue());
            }

            return ids;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public GroupCard getGroupCard(final long groupId){
        try {
            this.groupCardSelect.setLong(1, groupId);

            final ResultSet set = this.groupCardSelect.executeQuery();

            GroupCard card = null;

            while (set.next()) {
                card = JDBCSerializationSpecies.deserializeCard(this, set).orElse(null);
            }

            return card;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public GroupCard getGroupCard(final String groupTag){
        try {
            this.groupCardSelectTag.setString(1, groupTag);

            final ResultSet set = this.groupCardSelectTag.executeQuery();

            GroupCard card = null;

            while (set.next()) {
                card = JDBCSerializationSpecies.deserializeCard(this, set).orElse(null);
            }

            return card;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Long> getCacheableData(){
        try {
            final ResultSet set = this.sharedCacheable.executeQuery();
            final List<Long> list = new ArrayList<>();

            while (set.next()) {
                list.add(set.getLong(1));
            }

            return list;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public SharedAudio getAudio(final long internalId){
        try {
            this.sharedSelect.setLong(1, internalId);

            final ResultSet set = this.sharedSelect.executeQuery();

            SharedAudio audio = null;

            while (set.next()){
                audio = JDBCSerializationSpecies.deserializeAudio(this, set).orElse(null);
            }

            return audio;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DerivedMeta getMeta(final long internalId){
        try {
            this.metaSelect.setLong(1, internalId);

            return JDBCSerializationSpecies.deserializeMeta(this.metaSelect.executeQuery()).orElse(null);
        } catch (SQLException e){
            LOGGER.error("Can't locate meta (internal_id={})", internalId, e);
            return null;
        }
    }

    @Override
    public DerivedData getData(final long groupId, final long postId){
        try {
            this.dataSelect.setLong(1, groupId);
            this.dataSelect.setLong(2, postId);

            final ResultSet set = dataSelect.executeQuery();

            DerivedData data = null;

            while (set.next()){
                data = JDBCSerializationSpecies.deserializeData(this, set).orElse(null);
            }

            return data;
        } catch (SQLException e) {
            LOGGER.error("Error selecting data: {}:{}", groupId, postId, e);
            return null;
        }
    }

    @Override
    public DerivedData getData(long internalId) {
        try {
            this.dataByInternal.setLong(1, internalId);

            final ResultSet set = dataByInternal.executeQuery();

            DerivedData data = null;

            while (set.next()){
                data = JDBCSerializationSpecies.deserializeData(this, set).orElse(null);
            }

            return data;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public void updateData(final DerivedData data){
        try {
            this.dataUpdate.setString(1, data.getFileUniqueId());
            this.dataUpdate.setString(2, data.getRemoteFileId());
            this.dataUpdate.setString(3, data.getFileId());
            this.dataUpdate.setString(4, data.getTrackName());
            this.dataUpdate.setString(5, data.getTrackDuration());
            this.dataUpdate.setString(6, data.getTrackPerformer());
            this.dataUpdate.setString(7, data.getAggregator());
            this.dataUpdate.setString(8, data.getSchema());
            this.dataUpdate.setTimestamp(9, data.getCreationTimestamp());
            this.dataUpdate.setTimestamp(10, data.getModificationTimestamp());

            this.dataUpdate.setLong(11,data.getDerivedMeta().getInternalId());
            this.dataUpdate.executeUpdate();

            this.updateMeta(data.getDerivedMeta());
        } catch (Exception e){
            LOGGER.error("Can't update meta.", e);
        }
    }

    @Override
    public void updateMeta(final DerivedMeta meta){
        try {
            this.metaUpdate.setObject(1, meta.getMeta());
            this.metaUpdate.setLong(2,meta.getInternalId());
            this.metaUpdate.executeUpdate();
        } catch (Exception e){
            LOGGER.error("Can't update meta.", e);
        }
    }

    @Override
    public void saveMeta(final DerivedMeta meta){
        try {
            this.metaUpsert.setLong(1,meta.getInternalId());
            this.metaUpsert.setObject(2, meta.getMeta());
            this.metaUpsert.setString(3, meta.getSchema());
            this.metaUpsert.executeUpdate();
        } catch (Exception e){
            LOGGER.error("Can't save meta.", e);
        }
    }

    @Override
    public boolean containsData(final long groupId, final long postId){
        try {
            this.dataContains.setLong(1, groupId);
            this.dataContains.setLong(2, postId);

            final ResultSet set = this.dataContains.executeQuery();

            if (set.next())
                return set.getBoolean("exists");
            return false;
        } catch (Exception e){
            LOGGER.error("Can't check whether data exists.", e);
            return false;
        }
    }

    @Override
    public boolean containsData(long groupId, long postId, String trackName, String trackDuration) {
        try {
            this.sharedExists1.setLong(1, groupId);
            this.sharedExists1.setLong(2, postId);
            this.sharedExists1.setString(3, trackName);
            this.sharedExists1.setString(4, trackDuration);

            final ResultSet set = this.dataContains.executeQuery();

            if (set.next())
                return set.getBoolean("exists");
            return false;
        } catch (Exception e){
            LOGGER.error("Can't check whether data exists.", e);
            return false;
        }
    }

    @Override
    public Collection<DerivedData> search(final String query){
        final Collection<DerivedData> messages = new ArrayList<>();

        try {
            final ResultSet set = search.executeQuery(String.format(PGQuery.POSTGRE_SEARCH, query));

            while (set.next()){
                messages.add(JDBCSerializationSpecies.deserializeData(this, set).orElse(null));
            }

            return messages;
        } catch (Exception e){
            LOGGER.error("Can't execute {}", query, e);
            return Collections.emptyList();
        }
    }

    @Override
    public void close() throws Exception {

    }
}
