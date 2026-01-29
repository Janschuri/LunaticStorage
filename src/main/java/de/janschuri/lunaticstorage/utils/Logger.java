package de.janschuri.lunaticstorage.utils;


import de.janschuri.lunaticlib.utils.impl.LunaticLogger;
import de.janschuri.lunaticstorage.LunaticStorage;

public class Logger {

    private static final de.janschuri.lunaticlib.utils.Logger logger = LunaticLogger.getLogger("LunaticStorage");

    public static boolean isDebug() {
        return LunaticStorage.isDebug();
    }

    public static void debug(String msg) {
        if (isDebug()) {
            logger.debug(msg);
        }
    }

    public static void infoLog(String msg) {
        logger.info(msg);
    }

    public static void warnLog(String msg) {
        logger.warn(msg);
    }

    public static void errorLog(String msg) {
        logger.error(msg);
    }

}
