package massim.monitor;

/**
 * Main class of the monitor.
 * Starts the monitor in replay mode.
 */
public class Main {

    public static void main(String[] args){
        if(args.length != 1) System.out.println("Must be called with 1 argument");
        new Monitor(8000, args[0]);
    }
}
