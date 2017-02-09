package massim.scenario.city;

import massim.Server;
import massim.config.ServerConfig;
import massim.messages.Message;
import massim.messages.SimStartContent;
import massim.messages.RequestActionContent;
import massim.scenario.city.percept.CityInitialPercept;
import massim.scenario.city.percept.CityStepPercept;
import massim.util.Conversions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

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

    @Test //TODO make this more of a real test
    public void checkPercepts() throws Exception {
        JSONArray matches = jsonConfig.optJSONArray("match");
        assert(matches != null && matches.length() > 0);

        // check initial percept
        Map<String, SimStartContent> percepts = sim.init(100, matches.getJSONObject(0),
                new HashSet<>(serverConfig.teams.subList(0, serverConfig.teamsPerMatch))); //just use the first teams
        CityInitialPercept percept = (CityInitialPercept) percepts.values().iterator().next();
        long time = System.currentTimeMillis();
        Message originalMsg = new Message(time, percept);
        Document doc = originalMsg.toXML(CityInitialPercept.class);
        String perceptString = Conversions.docToString(doc, true);
        Message parsedMessage = Message.parse(doc, CityInitialPercept.class);
        assert(parsedMessage != null && parsedMessage.getContent() instanceof CityInitialPercept);
        CityInitialPercept recreatedPercept = (CityInitialPercept) parsedMessage.getContent();
        assert(percept.getId().equals(recreatedPercept.getId()));

        String recreatedPerceptString = Conversions.docToString(new Message(time, recreatedPercept).toXML(CityInitialPercept.class), true);
        assert(perceptString.equals(recreatedPerceptString));

        assert(percept.getItemData().size() == recreatedPercept.getItemData().size());
        assert(percept.getMapName().equals(recreatedPercept.getMapName()));

        // prepare the world a bit
        assert(sim.simGive("item0", "agentA1", 1));
        assert(sim.simStore("Storage1", "item0", "A", 3));
//        sim.simAddJob();

        // check step percept
        Map<String, RequestActionContent> stepPercepts = sim.preStep(0);
        RequestActionContent stepPercept = stepPercepts.get("agentA1");
        assert(stepPercept instanceof CityStepPercept);
        Message outgoing = new Message(System.currentTimeMillis(), stepPercept);
        doc = outgoing.toXML(CityStepPercept.class);
        System.out.println(Conversions.docToString(doc, true));
    }

    @Test
    public void finishesWithCorrectRanking() throws Exception {
        // add a few teamstates and check rankings
        //TODO
    }
}