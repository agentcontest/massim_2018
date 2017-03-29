package massim.monitor;

import massim.protocol.WorldData;
import massim.protocol.scenario.city.data.DynamicCityData;
import massim.protocol.scenario.city.data.StaticCityData;

import org.webbitserver.WebServers;
import org.webbitserver.WebServer;
import org.webbitserver.handler.StaticFileHandler;
import org.webbitserver.handler.HttpToWebSocketHandler;

import java.util.concurrent.ExecutionException;

/**
 * The web monitor for the MASSim server.
 */
public class Monitor {

    private final SocketHandler socketHandler = new SocketHandler();

    /**
     * Constructor.
     * Used by the massim server to create the "live" monitor.
     */
    public Monitor() throws ExecutionException, InterruptedException {
        WebServer server = WebServers.createWebServer(7777)
            .add("/socket", new HttpToWebSocketHandler(this.socketHandler))
            .add(new StaticFileHandler("webmonitor/www"))
            .start()
            .get();
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
        if (worldData instanceof StaticCityData) {
            // TODO
        } else if (worldData instanceof DynamicCityData){
            // TODO
        } else{
            System.out.println("Monitor: wrong scenario");
            return;
        }
    }
}
