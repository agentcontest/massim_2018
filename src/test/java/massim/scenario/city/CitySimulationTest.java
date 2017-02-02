package massim.scenario.city;

import massim.Server;
import massim.config.ServerConfig;
import massim.messages.SimStartPercept;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;

/**
 * Testing the scenario's main class.
 */
public class CitySimulationTest {

    private CitySimulation sim;
    private JSONObject jsonConfig;
    private ServerConfig serverConfig;

    @Before
    public void setUp() throws Exception {
        sim = new CitySimulation();
        Path cwd = Paths.get("").toAbsolutePath();
        Path confPath = cwd.resolve("conf/TestConfig.json");
        jsonConfig = new JSONObject(new String(Files.readAllBytes(confPath)));
        serverConfig = Server.parseServerConfig(jsonConfig);
    }

    @Test
    public void checkPercepts() throws Exception {
        JSONArray matches = jsonConfig.optJSONArray("match");
        assert(matches != null && matches.length() > 0);
        Map<String, SimStartPercept> percepts = sim.init(100, matches.getJSONObject(0),
                new HashSet<>(serverConfig.teams.subList(0, serverConfig.teamsPerMatch))); //just use the first teams
        System.out.println("yeah");
    }

}