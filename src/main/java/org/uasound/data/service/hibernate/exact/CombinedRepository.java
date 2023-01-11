package org.uasound.data.service.hibernate.exact;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.uasound.data.entity.DerivedData;
import org.uasound.data.entity.DerivedMeta;
import org.uasound.data.entity.GroupCard;
import org.uasound.data.entity.SharedAudio;
import org.uasound.data.service.hibernate.repository.HibernateInitializer;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class CombinedRepository implements AutoCloseable {
    private final SessionFactory sessionFactory;

    private final Session session;

    static final Lock LOCK = new ReentrantLock();

    static final String POSTGRE_SEARCH = "WITH reference (input) as (values (?))" +
            "select " +
            "internal_id, group_id, post_id, unique_file_id, " +
            "remote_file_id, file_id, track_name, track_duration, track_performer, aggregator, " +
            "\"schema\", creation_timestamp, modification_timestamp, similarity " +
            "from post_data, reference, " +
            "to_tsvector(post_data.track_name || post_data.track_performer) document, " +
            "to_tsquery(reference.input) query, " +
            "NULLIF(ts_rank(to_tsvector(post_data.track_name" +
            " || post_data.track_performer), query), 0) rank_title, " +
            "NULLIF(ts_rank(to_tsvector(post_data.track_name" +
            " || post_data.track_performer), query), 0) rank_description, " +
            "SIMILARITY(reference.input, post_data.track_name || post_data.track_performer) similarity " +
            "WHERE query @@ document OR similarity > 0 " +
            "ORDER BY rank_title, rank_description, similarity DESC NULLS LAST limit 5;";

    static final String SPECIAL_CHARACTERS = "[^\\p{L}\\p{Z}]";

    public CombinedRepository(final SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
        this.session = sessionFactory.openSession();
    }

    public CombinedRepository(){
        this(HibernateInitializer.current());
    }

    public void save(final DerivedMeta meta){
        session.persist(meta);
    }

    public void save(final GroupCard card){
        if (getGroupCard(card.getGroupId()) == null)
            session.persist(card);

    }

    public void save(final SharedAudio audio){
        if (!(containsAudio(audio.getInternalId())))
            session.persist(audio);
    }

    public void save(final DerivedData data){
        if (!(this.containsData(
                    data.getGroupId(),
                    data.getPostId(),
                    data.getTrackName(),
                    data.getTrackDuration())))
            session.persist(data);
    }

    public boolean containsData(final long internalId){
        return session.find(DerivedData.class, internalId) != null;
    }
    public boolean containsData(final long groupId,
                                final long postId,
                                final String trackName,
                                final String trackDuration){
        return session.createQuery("select exists(select internalId from DerivedData " +
                            "where groupId = :group_id and postId = :post_id and" +
                            " trackName = :track_name and trackDuration = :track_duration)", Boolean.class)
                    .setParameter("group_id", groupId)
                    .setParameter("post_id", postId)
                    .setParameter("track_name", trackName)
                    .setParameter("track_duration", trackDuration)
                    .getSingleResult();
    }

    public GroupCard getGroupCard(final long groupId){
        return session.find(GroupCard.class, groupId);
    }

    public GroupCard getGroupCard(final String groupTag){
        final Query<GroupCard> query = session
                .createQuery("from GroupCard where groupTag = :tag", GroupCard.class);

        query.setParameter("tag", groupTag);

        return query.getSingleResult();
    }

    public List<Long> cacheableData(){
        final Query<Long> query = session
                .createNativeQuery("select t1.internal_id from post_data t1 left join shared_audio" +
                        " t2 on t2.internal_id = t1.internal_id where t2.internal_id is null limit 5;", Long.class);
        return query.stream().limit(5).collect(Collectors.toList());
    }

    public List<Long> registeredGroups(){
        final List<? extends GroupCard> list = fetch(GroupCard.class);

        return list.stream().map(GroupCard::getGroupId).collect(Collectors.toList());
    }
    public boolean containsAudio(long internalId) {
        return session
                .createQuery("select exists(from SharedAudio where internalId = :internal_id)", Boolean.class)
                .setParameter("internal_id", internalId)
                .getSingleResult();
    }

    public SharedAudio getAudio(final long internalId){
        return session
                    .createQuery("from SharedAudio where internalId = :internal_id", SharedAudio.class)
                    .setParameter("internal_id", internalId).getSingleResult();
    }

    public DerivedMeta getMeta(long internalId) {
        return session.find(DerivedMeta.class, internalId);
    }

    public DerivedData getData(final long groupId, final long postId){
        final Query<DerivedData> query = session
                    .createQuery("from DerivedData where " +
                            "groupId = :group_id and postId = :post_id", DerivedData.class);

            query.setParameter("group_id", groupId);
            query.setParameter("post_id", postId);

            return query.getSingleResult();
    }

    public DerivedData getData(final long internalId){
        return sessionFactory.openSession().find(DerivedData.class, internalId);
    }

    public boolean containsData(final long groupId, final long postId){
            return session.createQuery("select exists(select internalId from DerivedData where " +
                    "groupId = :group_id and postId = :post_id)", Boolean.class)
                    .setParameter(1, groupId)
                    .setParameter(2, postId)
                    .getSingleResult();
    }

    public List<DerivedData> search(final String query){
        return session.createNativeQuery(POSTGRE_SEARCH, DerivedData.class)
                .setParameter(1, escapeString(query))
                .getResultList();
    }

    public String escapeString(final String input){
        return input.replaceAll(SPECIAL_CHARACTERS, "");
    }

    public <R> R transaction(final BiFunction<CombinedRepository, Session, R> body){
        final Session session = this.openTransaction();

        try {
            return body.apply(this, session);
        } catch (Throwable e) {
            e.printStackTrace();

            session.getTransaction().rollback();
            return null;
        } finally {
            session.getTransaction().commit();
            session.close();
            LOCK.unlock();
        }
    }

    public void transaction(final BiConsumer<CombinedRepository, Session> body){
        this.transaction((repository, session) -> {
            body.accept(repository, openTransaction());

            return null;
        });
    }

    public Session openTransaction(){
        // Different threads can use 'common' session when one is not locked by another,
        // so return 'ThreadLocal' (getCurrentSession) session.

        try {
            if (LOCK.tryLock()) {
                final Session newSession = sessionFactory.openSession();

                if (newSession.getTransaction() == null || !newSession.getTransaction().isActive())
                    newSession.beginTransaction();

                return newSession;
            }

            if (session.getTransaction() == null || !session.getTransaction().isActive()) {
                session.beginTransaction();
                return session;
            }
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public <T> List<? extends T> fetch(final Class<T> clazz){
        final CriteriaBuilder cb = session.getCriteriaBuilder();
        final CriteriaQuery<T> cq = cb.createQuery(clazz);

        final Root<T> root = cq.from(clazz);

        final CriteriaQuery<? extends T> all = cq.select(root)
                .orderBy(cb.asc(root.get("id")));

        return session.createQuery(all).getResultList();
    }

    @Override
    public void close() throws Exception {
        sessionFactory.close();
    }
}
