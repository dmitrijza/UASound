package org.uasound.data;

import org.uasound.data.service.hibernate.repository.DataProperty;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public final class ConfigurationResolver {
    private ConfigurationResolver(){ /* ... */ }

    public static DataProperty[] read(final Path path){
        try {
            final Properties properties = new Properties();
            properties.load(new FileReader(path.toFile()));

            return properties.entrySet().stream()
                    .map((entry) ->
                    DataProperty.of((String) entry.getKey(), (String) entry.getValue()))
                    .toArray(DataProperty[]::new);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
