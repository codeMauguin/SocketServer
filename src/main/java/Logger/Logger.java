package Logger;

import java.text.MessageFormat;
import java.time.LocalDateTime;

public class Logger {
    private final static String LOG_TEMPLATE = "%s%tF %tT [%s][%s]: %s\n";
    private final static String INFO = "\033[32m";
    private final static String WARN = "\033[33m";
    private final static String ERROR = "\033[31m";


    private static void log(final String msg, final String status) {
        StackTraceElement stackTraceElement = Thread.currentThread ( ).getStackTrace ( )[3];
        LocalDateTime now = LocalDateTime.now ( );
        System.out.format ( Logger.LOG_TEMPLATE, status, now, now,
                stackTraceElement.getClassName ( ) + ":" + stackTraceElement.getMethodName ( ),
                Thread.currentThread ( ).getName ( )+":"+ Thread.currentThread ( ).getId (), msg);
    }

    public static void info(final String msg) {
        Logger.log (msg,  Logger.INFO);
    }

    public static void info(final String msg, final Object... param) {
        Logger.log (MessageFormat.format (msg, param),  Logger.INFO);
    }

    public static void warn(final String warn) {
        Logger.log (warn,  Logger.WARN);
    }

    public static void error(final String error) {
        Logger.log (error,  Logger.ERROR);
    }
    
}
