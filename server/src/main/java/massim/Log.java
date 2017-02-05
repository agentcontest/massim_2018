package massim;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created in 2017.
 * TODO: just copied
 */
public class Log {
    public final static int CRITICAL = 400;
    public final static int ERROR = 300;
    public final static int NORMAL = 200;
    public final static int DEBUG = 100;

    private static OutputStream os_critical = System.err;
    private static OutputStream os_error = System.err;
    private static OutputStream os_normal = System.out;
    private static OutputStream os_debug = System.out;
    private static int thresholdcritical = 0;
    private static int thresholderror = 0;
    private static int thresholdnormal = 0;
    private static int thresholddebug = 0;

    private static File outputFile = null;
    private static FileWriter writer = null;

    private static synchronized void logString(int type, String s) {
        byte[] b=s.getBytes();
        String Debug    = "[ DEBUG  ] ";
        String Normal   = "[ NORMAL ] ";
        String Error    = "[ ERROR  ] ";
        String Critical = "[CRITICAL] ";
        try {
            if (type>= DEBUG +thresholddebug && type< NORMAL) {
                if (os_debug!=null) {
                    os_debug.write(Debug.getBytes());
                    os_debug.write(b);
                }
            } else
            if (type>= NORMAL +thresholdnormal && type< ERROR) {
                if (os_normal!=null) {
                    os_normal.write(Normal.getBytes());
                    os_normal.write(b);
                }
            } else
            if (type>= ERROR +thresholderror && type< CRITICAL) {
                if (os_error!=null) {
                    os_error.write(Error.getBytes());
                    os_error.write(b);
                }
            } else
            if (type>= CRITICAL +thresholdcritical) {
                if (os_critical!=null) {
                    os_critical.write(Critical.getBytes());
                    os_critical.write(b);
                }
            }

            String typeString;
            switch (type){
                case CRITICAL:
                    typeString = Critical;
                    break;

                case DEBUG:
                    typeString = Debug;
                    break;

                case NORMAL:
                    typeString = Normal;
                    break;

                case ERROR:
                    typeString = Error;
                    break;
                default:
                    typeString="";
            }
            logToFile(typeString + " " + s);

        } catch (IOException e) {
            System.err.println("Error while trying to write log string: "+s);
        }
    }
    private static String getMetaInfo() {
        //Get stack frame
        Exception e = new Exception();
        e.fillInStackTrace();
        StackTraceElement[] stack = e.getStackTrace();

        //Get time&date
        GregorianCalendar calendar = new GregorianCalendar();

        //Assemble string
        String t = "";
        String x="";
        // 2 means that this method and the calling method are ignored.
        // please note that this implies that this any method that calls getMetaInfo
        // should have been called from outside.
        //for(int i=2;i<stack.length;i++)
        int i=2;
        {
            StackTraceElement ls = stack[i];
            t+=x;
            t+= ls.getClassName()+"."+ls.getMethodName()+":"+ls.getLineNumber();
            x="<-";
        }
        String s =
                String.format("%02d:%02d:%02d",
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        calendar.get(Calendar.SECOND)) + " " +
                        Thread.currentThread().getId() + " " +
                        t;
        return s;
    }

    public static void log(int type) {
        logString(type, getMetaInfo()+"\n");
    }

    public static void log(int type, String msg) {
        int length = 90;

        //check wether default debug level is set to normal, then deactivate metaInfo
        if (thresholddebug > 0) {
            length = 0;
        }
        String metaInfo = getMetaInfo();
        int metaInfoLength =  metaInfo.length();
        while (metaInfoLength < length) {
            metaInfo = metaInfo.concat(" ");
            metaInfoLength++;
        }
        logString(type, metaInfo.substring(0, length) +" ##   " + msg + "\n");
    }

    public static void setLogLevel(String debugLevel) {
        if (debugLevel.equalsIgnoreCase("normal")) {
            thresholddebug++;
        }
        if (debugLevel.equalsIgnoreCase("error")) {
            thresholddebug++;
            thresholdnormal++;
        }
        if (debugLevel.equalsIgnoreCase("critical")) {
            thresholddebug++;
            thresholdnormal++;
            thresholderror++;
        }
    }

    public static void setLogFile(File f){
        outputFile = f;
    }

    public static synchronized void logToFile(String s){

        if(outputFile == null){
            return;
        }

        if (writer == null){
            try {
                writer = new FileWriter(outputFile, true);
            } catch (IOException ignored) {}
        }

        try {
            if(writer != null){
                writer.append(s);
                writer.flush();
            }
        } catch (IOException ignored) {}
    }

}
