package de.janschuri.lunaticstorage.utils;


import de.janschuri.lunaticlib.common.logger.AbstractLogger;
import de.janschuri.lunaticstorage.LunaticStorage;

public class Logger extends AbstractLogger {

    private static final org.slf4j.Logger logger = Logger.getLogger("LunaticStorage");

    public static boolean isDebug() {
        return LunaticStorage.isDebug();
    }

    public static void debugLog(String msg) {
        if (isDebug()) {
            debug(logger, msg);
        }
    }

    public static void infoLog(String msg) {
        info(logger, msg);
    }

    public static void warnLog(String msg) {
        warn(logger, msg);
    }

    public static void errorLog(String msg) {
        error(logger, msg);
    }

}
