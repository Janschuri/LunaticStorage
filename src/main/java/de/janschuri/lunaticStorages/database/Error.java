package de.janschuri.lunaticStorages.database;

import de.janschuri.lunaticStorages.LunaticStorage;

import java.util.logging.Level;

public class Error {
    public static void execute(LunaticStorage plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }
    public static void close(LunaticStorage plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}