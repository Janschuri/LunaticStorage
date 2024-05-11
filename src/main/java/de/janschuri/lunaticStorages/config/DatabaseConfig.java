package de.janschuri.lunaticStorages.config;

import de.janschuri.lunaticStorages.utils.Logger;
import de.janschuri.lunaticlib.config.AbstractDatabaseConfig;

import java.nio.file.Path;

public class DatabaseConfig extends AbstractDatabaseConfig {

    private static final String NAME = "lunaticstorage";
    private static final String DATABASE_FILE = "database.yml";
    private static final String DEFAULT_DATABASE_FILE = "database.yml";

    public DatabaseConfig(Path dataDirectory) {
        super(NAME, dataDirectory, DATABASE_FILE, DEFAULT_DATABASE_FILE);
        load();
    }
}
