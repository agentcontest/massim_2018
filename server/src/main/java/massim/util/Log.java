package massim.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Logger utility for the MASSim server. Supports 4 different log levels.
 */
public class Log {

    public enum Level { CRITICAL, ERROR, NORMAL, DEBUG }

    private static Level logLevel = Level.NORMAL;
    private static File outputFile = null;
    private static FileWriter writer = null;

    private static Map<Level, OutputStream> outputs = new HashMap<>();
    private static Map<Level, String> typeStrings = new HashMap<>();

    static{ // initialization
        outputs.put(Level.CRITICAL, System.err);
        outputs.put(Level.ERROR, System.err);
        outputs.put(Level.NORMAL, System.out);
        outputs.put(Level.DEBUG, System.out);

        typeStrings.put(Level.CRITICAL, "[ CRITICAL  ] ");
        typeStrings.put(Level.ERROR, "[ ERROR  ] ");
        typeStrings.put(Level.NORMAL, "[ NORMAL  ] ");
        typeStrings.put(Level.DEBUG, "[ DEBUG  ] ");

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            logToFile(typeStrings.get(Level.ERROR) + e + " : " + Arrays.toString(e.getStackTrace()));
        });
    }

    /**
     * Logs a string with a certain log level to the respective output stream if the current log level permits.
     * @param level the log level at which to log
     * @param message the message to log
     */
    private static synchronized void logString(Level level, String message) {
        if (level.ordinal() <= logLevel.ordinal()){
            OutputStream out = outputs.get(level);
            String typeString = typeStrings.get(level);
            try {
                out.write(typeString.getBytes());
                out.write(message.getBytes());
                logToFile(typeString + message);
            } catch (IOException e) {
                System.err.println("Error while trying to write log string: " + message);
            }
        }
    }

    /**
     * Logs a string at the given log level if the level is currently being logged.
     * In {@link Level#DEBUG}, some meta info is prepended.
     * @param type the log level to use
     * @param msg the message to log
     */
    public static void log(Level type, String msg) {

        String metaInfo = "";
        if (logLevel == Level.DEBUG){
            int maxMetaLength = 90;
            metaInfo = getMetaInfo();
            if (metaInfo.length() < maxMetaLength){
                char[] ws = new char[maxMetaLength - metaInfo.length()];
                Arrays.fill(ws, ' ');
                metaInfo = metaInfo + new String(ws);
            }
        }

        logString(type, metaInfo +" ##   " + msg + "\n");
    }

    /**
     * Sets the current log level.
     * @param level the level determining the log messages to display.
     */
    public static void setLogLevel(Level level) {
        logLevel = level;
    }

    /**
     * Sets the file to output the log to.
     * @param f the file to write log entries to
     */
    public static void setLogFile(File f){
        outputFile = f;
    }

    /**
     * Changes the output stream for a given log level.
     * @param level the level to change the log destination of
     * @param out the new output stream
     */
    public void changeOutputStream(Level level, OutputStream out){
        if(out != null){
            outputs.put(level, out);
        }
    }

    /**
     * @return some meta information about the current context
     */
    private static String getMetaInfo() {
        Exception e = new Exception();
        e.fillInStackTrace();
        StackTraceElement[] stack = e.getStackTrace();
        GregorianCalendar calendar = new GregorianCalendar();
        String t = "";
        String x = "";
        /*
         * 2 means this method and the calling method are ignored. thus, any method
         * calling getMetaInfo should have been called from outside.
         */
        StackTraceElement ls = stack[2];
        t += x;
        t += ls.getClassName() + "." + ls.getMethodName() + ":" + ls.getLineNumber();
        return String.format("%02d:%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND)) + " " +
                Thread.currentThread().getId() + " " +
                t;
    }

    /**
     * Writes a string to the log file.
     * @param s the string to write
     */
    private static synchronized void logToFile(String s){

        if(outputFile == null) return;

        if (writer == null){
            try {
                writer = new FileWriter(outputFile, true);
            } catch (IOException ignored) {}
        }

        if(writer != null){
            try {
                writer.append(s);
                writer.flush();
            } catch (IOException ignored) {}
        }
    }
}
