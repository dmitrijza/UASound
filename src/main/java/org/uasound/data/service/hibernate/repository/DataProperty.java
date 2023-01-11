package org.uasound.data.service.hibernate.repository;

public class DataProperty {
    public enum PropertySource { SYSTEM, PLAIN }
    private final PropertySource source;
    private final String key;
    protected final String value;

    public DataProperty(final PropertySource source, final String key, final String value) {
        this.source = source;
        this.key = key;
        this.value = value;
    }

    public DataProperty(final String key, final String value) {
        this(PropertySource.PLAIN, key, value);
    }

    public String key() {
        return key;
    }

    public String value() {
        if (this.source.equals(PropertySource.SYSTEM))
            return System.getenv(this.value);

        return value;
    }

    public PropertySource source() {
        return source;
    }

    public static DataProperty of(final String key, final String value){
        return new DataProperty(key, value);
    }

    public static DataProperty system(final String key, final String value){
        return new DataProperty(PropertySource.SYSTEM, key, value);
    }
}
