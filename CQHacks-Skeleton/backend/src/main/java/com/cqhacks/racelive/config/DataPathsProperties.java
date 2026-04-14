package com.cqhacks.racelive.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataPathsProperties {

    /** Directory containing normalized CSV tables (Driver.csv, Race.csv, …). */
    private final Path dataDirectory;

    public DataPathsProperties(@Value("${app.data-dir:}") String dataDir) {
        this.dataDirectory = resolvePath(dataDir, Path.of("..", "data"));
    }

    private static Path resolvePath(String configured, Path defaultRelativeToBackend) {
        if (configured != null && !configured.isBlank()) {
            return Path.of(configured).toAbsolutePath().normalize();
        }
        return defaultRelativeToBackend.toAbsolutePath().normalize();
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }
}
