package org.uasound.data.service.hibernate;

import org.uasound.data.ConfigurationResolver;
import org.uasound.data.entity.*;
import org.uasound.data.service.DataService;
import org.uasound.data.service.hibernate.exact.CombinedRepository;
import org.uasound.data.service.hibernate.repository.HibernateInitializer;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

public class HibernateDataService implements DataService {
    private CombinedRepository repository;

    @Override
    public void init() {
        HibernateInitializer.install(
                "org.uasound.data.entity",
                        ConfigurationResolver.read(Paths.get("configuration/data.properties"))
                );

        this.repository = new CombinedRepository();
    }

    @Override
    public void saveAlbum(DerivedAlbum album) {

    }

    @Override
    public void saveLinkage(AlbumLinkage linkage) {

    }

    @Override
    public DerivedAlbum getAlbum(long internalId) {
        return null;
    }

    @Override
    public DerivedAlbum getAlbum(String author, String name, int year) {
        return null;
    }

    @Override
    public void saveGroupCard(GroupCard card) {
        repository.save(card);
    }

    @Override
    public void saveData(DerivedData derivedData) {
        repository.save(derivedData);
    }

    @Override
    public void updateData(DerivedData data) {
        repository.save(data);
    }

    @Override
    public void saveAudio(SharedAudio audio) {
        repository.save(audio);
    }

    @Override
    public void saveMeta(DerivedMeta meta) {
        repository.save(meta);
    }

    @Override
    public void updateMeta(DerivedMeta meta) {
        repository.save(meta);
    }

    @Override
    public boolean containsAudio(long internalId) {
        return repository.containsAudio(internalId);
    }

    public boolean containsData(final long groupId,
                                final long postId,
                                final String trackName,
                                final String trackDuration){
        return repository.containsData(groupId, postId, trackName, trackDuration);
    }

    @Override
    public List<Long> getGroupIdList() {
        return this.repository.registeredGroups();
    }

    @Override
    public GroupCard getGroupCard(long groupId) {
        return repository.getGroupCard(groupId);
    }

    @Override
    public GroupCard getGroupCard(String groupTag) {
        return repository.getGroupCard(groupTag);
    }

    @Override
    public List<Long> getCacheableData() {
        return repository.cacheableData();
    }

    @Override
    public SharedAudio getAudio(long internalId) {
        return repository.getAudio(internalId);
    }

    @Override
    public DerivedMeta getMeta(long internalId) {
        return repository.getMeta(internalId);
    }

    @Override
    public DerivedData getData(long groupId, long postId) {
        return repository.getData(groupId, postId);
    }


    @Override
    public DerivedData getData(long internalId) {
        return repository.getData(internalId);
    }

    @Override
    public AlbumLinkage getLinkageOf(long internalId) {
        return null;
    }

    @Override
    public boolean containsData(long groupId, long postId) {
        return repository.containsData(groupId, postId);
    }

    @Override
    public Collection<DerivedData> search(String query) {
        return repository.search(query);
    }

    @Override
    public void close() throws Exception {
        this.repository.close();
    }
}
