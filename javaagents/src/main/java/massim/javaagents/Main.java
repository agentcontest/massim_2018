package massim.javaagents;

import eis.EILoader;
import eis.EnvironmentInterfaceStandard;
import eis.exceptions.ManagementException;
import eis.iilang.EnvironmentState;

import java.io.IOException;

/**
 * Starts a new scheduler.
 */
public class Main {

    public static void main( String[] args ) {

        System.out.println("PHASE 1: INSTANTIATING SCHEDULER");
        Scheduler scheduler = null;
        if (args.length != 0) scheduler = new Scheduler(args[0]);
        else scheduler = new Scheduler();

        System.out.println("PHASE 2: INSTANTIATING ENVIRONMENT");
        EnvironmentInterfaceStandard ei = null;
        try {
            ei = EILoader.fromClassName("massim.EnvironmentInterface");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        try {
            ei.start();
        } catch (ManagementException e) {
            e.printStackTrace();
        }

        System.out.println("PHASE 3: CONNECTING SCHEDULER AND ENVIRONMENT");
        scheduler.setEnvironment(ei);

        System.out.println("PHASE 4: RUNNING");
        int step = 0;
        while (!(ei.getState() == EnvironmentState.KILLED)) {
            System.out.println("SCHEDULER STEP " + step);
            scheduler.step();
            step++;
        }
    }
}
