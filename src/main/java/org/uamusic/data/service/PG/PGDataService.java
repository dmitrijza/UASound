package org.uamusic.data.service.PG;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uamusic.data.JDBCSerializationSpecies;
import org.uamusic.data.entity.DerivedData;
import org.uamusic.data.entity.DerivedMeta;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Implementation of DataService that stores data in PostgresSQL.
 * Hardcode at its best, needs to be replaced to canonical type of storage possession.
 */
public final class PGDataService {
    @Getter
    private HikariDataSource dataSource;

    private PreparedStatement
            dataUpsert, dataUpdate, dataContains, metaUpdate, metaUpsert, metaSelect, dataSelect;

    private Statement search;

    static final Logger LOGGER = LoggerFactory.getLogger(PGDataService.class);

    public void init(){
        final HikariConfig config = new HikariConfig("settings/data.properties");
        this.dataSource = new HikariDataSource(config);

        try {
            final Connection connection = this.dataSource.getConnection();
            final Statement prepareStatement = connection.createStatement();

            prepareStatement.executeUpdate(PGQuery.POSTGRE_DATA_TABLE);
            prepareStatement.executeUpdate(PGQuery.POSTGRE_META_TABLE);

            this.search = connection.createStatement();

            this.dataContains = connection.prepareStatement(PGQuery.POSTGRE_DATA_CONTAINS);
            this.dataSelect = connection.prepareStatement(PGQuery.POSTGRE_DATA_SELECT);
            this.dataUpdate = connection.prepareStatement(PGQuery.POSTGRE_DATA_UPDATE);
            this.metaUpdate = connection.prepareStatement(PGQuery.POSTGRE_META_UPDATE);
            this.metaUpsert = connection.prepareStatement(PGQuery.POSTGRE_META_UPSERT);
            this.dataUpsert = connection.prepareStatement(PGQuery.POSTGRE_MESSAGE_UPSERT, Statement.RETURN_GENERATED_KEYS);
            this.metaSelect = connection.prepareStatement(PGQuery.POSTGRE_META_SELECT, Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveData(final DerivedData derivedData){
        if (containsData(BigDecimal.valueOf(derivedData.getGroupId()), BigDecimal.valueOf(derivedData.getPostId()))) {
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

    public DerivedMeta getMeta(final BigDecimal internalId){
        try {
            this.metaSelect.setBigDecimal(1, internalId);

            return JDBCSerializationSpecies.deserializeMeta(this.metaSelect.executeQuery()).orElse(null);
        } catch (SQLException e){
            LOGGER.error("Can't locate meta (internal_id={})", internalId, e);
            return null;
        }
    }

    public DerivedData getData(final BigDecimal groupId, final BigDecimal postId){
        try {
            this.dataSelect.setBigDecimal(1, groupId);
            this.dataSelect.setBigDecimal(2, postId);

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

    public void updateData(final DerivedData data){
        try {
            this.dataUpdate.setString(1, data.getFileId());
            this.dataUpdate.setString(2, data.getTrackName());
            this.dataUpdate.setString(3, data.getTrackDuration());
            this.dataUpdate.setString(4, data.getTrackPerformer());
            this.dataUpdate.setString(5, data.getAggregator());
            this.dataUpdate.setString(6, data.getSchema());
            this.dataUpdate.setTimestamp(7, data.getCreationTimestamp());
            this.dataUpdate.setTimestamp(8, data.getModificationTimestamp());

            this.dataUpdate.setBigDecimal(9, BigDecimal.valueOf(data.getDerivedMeta().getInternalId()));
            this.dataUpdate.executeUpdate();

            this.updateMeta(data.getDerivedMeta());
        } catch (Exception e){
            LOGGER.error("Can't update meta.", e);
        }
    }

    public void updateMeta(final DerivedMeta meta){
        try {
            this.metaUpdate.setObject(1, meta.getMeta());
            this.metaUpdate.setBigDecimal(2, BigDecimal.valueOf(meta.getInternalId()));
            this.metaUpdate.executeUpdate();
        } catch (Exception e){
            LOGGER.error("Can't update meta.", e);
        }
    }

    public void saveMeta(final DerivedMeta meta){
        try {
            this.metaUpsert.setBigDecimal(1, BigDecimal.valueOf(meta.getInternalId()));
            this.metaUpsert.setObject(2, meta.getMeta());
            this.metaUpsert.setString(3, meta.getSchema());
            this.metaUpsert.executeUpdate();
        } catch (Exception e){
            LOGGER.error("Can't save meta.", e);
        }
    }

    public boolean containsData(final BigDecimal groupId, final BigDecimal postId){
        try {
            this.dataContains.setBigDecimal(1, groupId);
            this.dataContains.setBigDecimal(2, postId);

            final ResultSet set = this.dataContains.executeQuery();

            if (set.next())
                return set.getBoolean("exists");
            return false;
        } catch (Exception e){
            LOGGER.error("Can't check whether data exists.", e);
            return false;
        }
    }

    public Collection<DerivedData> search(final String query){
        final Collection<DerivedData> messages = new ArrayList<>();

        try {
            final ResultSet set = search.executeQuery(String.format(PGQuery.POSTGRE_SEARCH, query, query));

            while (set.next()){
                messages.add(JDBCSerializationSpecies.deserializeData(this, set).orElse(null));
            }

            return messages;
        } catch (Exception e){
            LOGGER.error("Can't execute {}", query, e);
            return Collections.emptyList();
        }
    }
}
