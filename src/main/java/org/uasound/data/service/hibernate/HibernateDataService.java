package org.uasound.data.service.hibernate;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.uasound.data.JDBCSerializationSpecies;
import org.uasound.data.entity.*;
import org.uasound.data.service.DataService;
import org.uasound.data.service.hibernate.exact.*;

import java.util.Collection;
import java.util.List;

// Semi-implemented {@link DataService} for Hibernate
@Service
public class HibernateDataService implements DataService {
    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private AudioRepository audioRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    private LinkageRepository linkageRepository;

    @Autowired
    private DataMetaRepository metaRepository;

    @Override
    public void init() {

    }

    @Override
    public void saveAlbum(DerivedAlbum album) {
        this.albumRepository.save(album);
    }

    @Override
    public void saveLinkage(AlbumLinkage linkage) {
        this.linkageRepository.save(linkage);
    }

    @Override
    public DerivedAlbum getAlbum(long internalId) {
        return this.albumRepository.findById(internalId).orElse(null);
    }

    @Override
    public DerivedAlbum getAlbum(String author, String name, int year) {
        return this.albumRepository.findDerivedAlbumByAuthorAndNameAndYear(author, name, year).orElse(null);
    }

    @Override
    public void saveGroupCard(GroupCard card) {
        this.cardRepository.save(card);
    }

    @Override
    public void saveData(DerivedData derivedData) {
        if (this.dataRepository.existsByGroupIdAndPostId(derivedData.getGroupId(), derivedData.getPostId()))
            return;

        this.dataRepository.save(derivedData);
    }

    @Override
    public void updateData(DerivedData data) {
        this.dataRepository.save(data);
    }

    @Override
    public void saveAudio(SharedAudio audio) {
        this.audioRepository.save(audio);
    }

    @Override
    public void saveMeta(DerivedMeta meta) {
        this.metaRepository.save(meta);
    }

    @Override
    public void updateMeta(DerivedMeta meta) {
        this.metaRepository.save(meta);
    }

    @Override
    public boolean containsAudio(long internalId) {
        return this.audioRepository.existsById(internalId);
    }

    public boolean containsData(final long groupId,
                                final long postId,
                                final String trackName,
                                final String trackDuration){
        return this.dataRepository
                .existsByGroupIdAndPostIdAndTrackNameAndTrackDuration(groupId, postId, trackName, trackDuration);
    }

    @Override
    public List<Long> getGroupIdList() {
        return Lists.newArrayList(this.cardRepository.findAllIds());
    }

    @Override
    public GroupCard getGroupCard(long groupId) {
        return this.cardRepository.getGroupCardByGroupId(groupId);
    }

    @Override
    public GroupCard getGroupCard(String groupTag) {
        return this.cardRepository.getGroupCardByGroupTag(groupTag);
    }

    @Override
    public List<Long> getCacheableData() {
        return Lists.newArrayList(this.audioRepository.cacheableData());
    }

    @Override
    public SharedAudio getAudio(long internalId) {
        return this.audioRepository.getAudioByInternalId(internalId);
    }

    @Override
    public DerivedMeta getMeta(long internalId) {
        return this.metaRepository.getMetaByInternalId(internalId);
    }

    @Override
    public DerivedData getData(long groupId, long postId) {
        return this.dataRepository.getDataByGroupIdAndPostId(groupId, postId);
    }


    @Override
    public DerivedData getData(long internalId) {
        return this.dataRepository.getDataByInternalId(internalId);
    }

    @Override
    public DerivedData getData(String fileId) {
        return this.dataRepository.getDataByFileId(fileId);
    }

    @Override
    public DerivedData getDataUnique(String uniqueFileId) {
        return this.dataRepository.getDataByFileUniqueId(uniqueFileId);
    }

    @Override
    public AlbumLinkage getLinkageOf(long internalId) {
        return this.linkageRepository.findAlbumLinkageByDataId(internalId).orElse(null);
    }

    @Override
    public Collection<AlbumLinkage> getLinkedFiles(long albumId) {
        return Lists.newArrayList(this.linkageRepository.findAllByAlbumId(albumId));
    }

    @Override
    public boolean containsData(long groupId, long postId) {
        return this.dataRepository.existsByGroupIdAndPostId(groupId, postId);
    }

    @Override
    public Collection<DerivedData> search(String query) {
        return this.dataRepository.search(JDBCSerializationSpecies.escapeQuery(query));
    }

    @Override
    public Collection<DerivedAlbum> searchAlbum(String query) {
        return Lists.newArrayList(this.albumRepository.searchAlbum(JDBCSerializationSpecies.escapeQuery(query)));
    }

    @Override
    public Collection<DerivedAlbum> searchAlbumAuthor(String query) {
        return Lists.newArrayList(this.albumRepository.searchAlbumAuthor(JDBCSerializationSpecies.escapeQuery(query)));
    }
}
