package massim.javaagents;

import eis.EILoader;
import eis.EnvironmentInterfaceStandard;
import eis.exceptions.ManagementException;
import eis.iilang.EnvironmentState;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Starts a new scheduler.
 */
public class Main {

    public static void main( String[] args ) {

        System.out.println("PHASE 1: INSTANTIATING SCHEDULER");
        Scheduler scheduler = null;
        if (args.length != 0) scheduler = new Scheduler(args[0]);
        else {
            System.out.println("PHASE 1.2: CHOOSE CONFIGURATION");
            File confDir = new File("conf");
            confDir.mkdirs();
            File[] confFiles = confDir.listFiles(File::isDirectory);
            if (confFiles == null || confFiles.length == 0) {
                System.out.println("No javaagents config files available - exit JavaAgents.");
                System.exit(0);
            }
            else {
                System.out.println("Choose a number:");
                for (int i = 0; i < confFiles.length; i++) {
                    System.out.println(i + " " + confFiles[i]);
                }
                Scanner in = new Scanner(System.in);
                Integer confNum = null;
                while (confNum == null) {
                    try {
                        confNum = Integer.parseInt(in.next());
                        if (confNum < 0 || confNum > confFiles.length - 1){
                            System.out.println("No config for that number, try again:");
                            confNum = null;
                        }
                    } catch (Exception e) {
                        System.out.println("Invalid number, try again:");
                    }
                }
                scheduler = new Scheduler(confFiles[confNum].getPath());
            }
        }

        System.out.println("PHASE 2: INSTANTIATING ENVIRONMENT");
        EnvironmentInterfaceStandard ei = null;
        try {
            ei = EILoader.fromClassName("massim.eismassim.EnvironmentInterface");
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
