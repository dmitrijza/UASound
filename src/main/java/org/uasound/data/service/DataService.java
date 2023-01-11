package org.uasound.data.service;

import org.uasound.data.entity.*;

import java.util.Collection;
import java.util.List;

public interface DataService extends AutoCloseable {
    void init() throws Throwable;

    void saveAlbum(final DerivedAlbum album);

    void saveLinkage(final AlbumLinkage linkage);

    DerivedAlbum getAlbum(final long internalId);

    DerivedAlbum getAlbum(final String author, final String name, final int year);

    void saveGroupCard(GroupCard card);

    void saveData(DerivedData derivedData);

    void saveAudio(SharedAudio audio);

    boolean containsAudio(long internalId);

    List<Long> getGroupIdList();

    GroupCard getGroupCard(long groupId);

    GroupCard getGroupCard(String groupTag);

    List<Long> getCacheableData();

    SharedAudio getAudio(long internalId);

    DerivedMeta getMeta(long internalId);

    DerivedData getData(long groupId, long postId);

    DerivedData getData(final long internalId);

    AlbumLinkage getLinkageOf(final long internalId);

    void updateData(DerivedData data);

    void updateMeta(DerivedMeta meta);

    void saveMeta(DerivedMeta meta);

    boolean containsData(long groupId, long postId);

    boolean containsData(final long groupId, final long postId, final String trackName, final String trackDuration);

    Collection<DerivedData> search(String query);

    Collection<DerivedAlbum> searchAlbum(String query);

    Collection<DerivedAlbum> searchAlbumAuthor(String query);

}
