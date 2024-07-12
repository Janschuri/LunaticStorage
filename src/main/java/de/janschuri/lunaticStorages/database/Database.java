package de.janschuri.lunaticStorages.database;

import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticStorages.config.DatabaseConfig;
import de.janschuri.lunaticStorages.database.tables.ChestsTable;
import de.janschuri.lunaticStorages.database.tables.PanelsTable;
import de.janschuri.lunaticlib.common.database.Table;


public abstract class Database {

    private static de.janschuri.lunaticlib.common.database.Database db;
    private static final Table[] tables = {
            ChestsTable.getTable(),
            PanelsTable.getTable(),
    };

    public static boolean loadDatabase() {
        DatabaseConfig databaseConfig = new DatabaseConfig(LunaticStorage.getDataDirectory());
        databaseConfig.load();
        db = de.janschuri.lunaticlib.common.database.Database.getDatabase(databaseConfig, tables);

        return db != null;
    }

    public static de.janschuri.lunaticlib.common.database.Database getDatabase() {
        return db;
    }
}
