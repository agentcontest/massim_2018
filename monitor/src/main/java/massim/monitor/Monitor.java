package massim.monitor;

import massim.protocol.WorldData;
import massim.protocol.scenario.city.data.CityWorldData;

/**
 * The web monitor for the MASSim server.
 * Can also be used to watch replays.
 */
public class Monitor {

    /**
     * Constructor.
     * Used by the massim server to create the "live" monitor.
     */
    public Monitor(){
        // TODO
    }

    /**
     * Creates a new monitor to watch replays with.
     * @param replayPath the path to a replay file
     */
    Monitor(String replayPath){
        // TODO
    }

    /**
     * Updates the current state of the monitor.
     * Called by the massim server after each step.
     */
    public void updateState(WorldData worldData){
        if(!(worldData instanceof CityWorldData)){
            System.out.println("Monitor: wrong scenario");
            return;
        }

        CityWorldData world = (CityWorldData) worldData;
        // TODO
    }
}
