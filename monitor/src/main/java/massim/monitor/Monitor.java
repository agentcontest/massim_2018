package massim.monitor;

import massim.protocol.WorldData;
import massim.protocol.scenario.city.data.DynamicCityData;
import massim.protocol.scenario.city.data.StaticCityData;

import org.json.JSONObject;

import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.handler.HttpToWebSocketHandler;
import org.webbitserver.handler.EmbeddedResourceHandler;

import java.util.HashSet;
import java.util.concurrent.ExecutionException;

/**
 * The web monitor for the MASSim server.
 */
public class Monitor {

    private String latestStatic;
    private String latestDynamic;

    private final HashSet<WebSocketConnection> pool = new HashSet<WebSocketConnection>();

    private final BaseWebSocketHandler socketHandler = new BaseWebSocketHandler() {

        @Override
        public void onOpen(WebSocketConnection client) {
            pool.add(client);
            if (latestStatic != null) client.send(latestStatic);
            if (latestDynamic != null) client.send(latestDynamic);
            System.out.println(String.format("[ MONITOR ] %d viewer(s) connected", pool.size()));
        }

        @Override
        public void onClose(WebSocketConnection client) {
            pool.remove(client);
            System.out.println(String.format("[ MONITOR ] %d viewer(s) connected", pool.size()));
        }
    };

    /**
     * Constructor.
     * Used by the massim server to create the "live" monitor.
     */
    public Monitor(int port) throws ExecutionException, InterruptedException {
        WebServer server = WebServers.createWebServer(port)
            .add("/socket", new HttpToWebSocketHandler(this.socketHandler))
            .add(new EmbeddedResourceHandler("www"))
            .start()
            .get();

        System.out.println(String.format("[ MONITOR ] Webmonitor listening on http://localhost:%d/", port));
    }

    /**
     * Creates a new monitor to watch replays with.
     * @param replayPath the path to a replay file
     */
    Monitor(int port, String replayPath) {
        // TODO
    }

    private void broadcast(String message) {
        for (WebSocketConnection client: this.pool) {
            client.send(message);
        }
    }

    /**
     * Updates the current state of the monitor.
     * Called by the massim server after each step.
     */
    public void updateState(WorldData worldData){
        if (worldData instanceof StaticCityData) {
            this.latestStatic = staticToJson((StaticCityData) worldData);
            this.broadcast(this.latestStatic);
        } else if (worldData instanceof DynamicCityData) {
            this.latestDynamic = dynamicToJson((DynamicCityData) worldData);
            this.broadcast(this.latestDynamic);
        }
    }

    private String staticToJson(StaticCityData data) {
        JSONObject d = new JSONObject();
        d.put("simId", data.simId);
        d.put("steps", data.steps);
        d.put("map", data.map);
        d.put("seedCapital", data.seedCapital);
        d.put("teams", data.teams);
        d.put("roles", data.roles);
        d.put("items", data.items);
        return d.toString();
    }

    private String dynamicToJson(DynamicCityData data) {
        JSONObject d = new JSONObject();
        d.put("step", data.step);
        d.put("workshops", data.workshops);
        d.put("chargingStations", data.chargingStations);
        d.put("shops", data.shops);
        d.put("dumps", data.dumps);
        d.put("resourceNodes", data.resourceNodes);
        d.put("storages", data.storages);
        d.put("entities", data.entities);
        d.put("jobs", data.jobs);
        d.put("teams", data.teams);
        return d.toString();
    }
}
