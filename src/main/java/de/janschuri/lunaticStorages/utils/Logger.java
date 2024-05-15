package de.janschuri.lunaticStorages.utils;

import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticlib.logger.AbstractLogger;

public class Logger extends AbstractLogger {

    private static final org.slf4j.Logger logger = AbstractLogger.getLogger("LunaticStorage");

    public static boolean isDebug() {
        return true;
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
