package org.uasound.data.service.hibernate.repository;

import jakarta.persistence.Entity;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.reflections.Reflections;

import java.util.Set;

public class HibernateInitializer {
    private HibernateInitializer() { }

    static final String CURRENT_SESSION_CONTEXT = "org.hibernate.context.internal.ThreadLocalSessionContext";

    static SessionFactory _sessionFactory;

    static String DRIVER, URL, USER, PASSWORD;

    static DataProperty[] _properties;

    static Class<?>[] _entities;

    public static void install(final String entityPath, final DataProperty... properties){
        final Reflections reflections = new Reflections(entityPath);
        final Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);

        for (DataProperty property : properties) {
            switch (property.key()) {
                case Environment.DRIVER: { DRIVER = property.value(); break; }
                case Environment.URL: { URL = property.value(); break; }
                case Environment.USER: { USER = property.value(); break; }
                case Environment.PASS: { PASSWORD = property.value(); break; }
                default : {}
            }
        }

        _properties = properties;
        _entities = entities.toArray(new Class[]{});
    }

    public static SessionFactory current(){
        if (_sessionFactory != null)
            return _sessionFactory;

        final Configuration configuration = new Configuration();

        configuration.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, CURRENT_SESSION_CONTEXT);

        configuration.setProperty(Environment.DRIVER, DRIVER);

        configuration.setProperty(Environment.URL, URL);
        configuration.setProperty(Environment.USER, USER);
        configuration.setProperty(Environment.PASS, PASSWORD);

        for (Class<?> entityClass : _entities)
            configuration.addAnnotatedClass(entityClass);

        _sessionFactory = configuration.buildSessionFactory();

        return _sessionFactory;
    }

}
