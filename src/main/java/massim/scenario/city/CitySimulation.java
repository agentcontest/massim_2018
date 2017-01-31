package massim.scenario.city;

import massim.Action;
import massim.Log;
import massim.Percept;
import massim.RNG;
import massim.config.TeamConfig;
import massim.scenario.AbstractSimulation;
import massim.scenario.city.data.WorldState;
import massim.scenario.city.percept.InitialPercept;
import massim.scenario.city.util.Generator;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Main class of the City scenario (2017).
 * @author ta10
 */
public class CitySimulation extends AbstractSimulation {

    private WorldState world;
    private ActionExecutor actionExecutor;
    private Generator generator;

    @Override
    public Map<String, Percept> init(int steps, JSONObject config, Set<TeamConfig> matchTeams) {

        // build the random generator
        JSONObject randomConf = config.optJSONObject("generate");
        if(randomConf == null){
            Log.log(Log.ERROR, "No random generation parameters!");
            randomConf = new JSONObject();
        }
        generator = new Generator(randomConf);

        // create the most important things
        world = new WorldState(steps, config, matchTeams, generator);
        actionExecutor = new ActionExecutor(world);

        // determine initial percepts
        Map<String, Percept> initialPercepts = new HashMap<>();
        world.getAgents().forEach(agName -> initialPercepts.put(agName, new InitialPercept(agName, world)));
        return initialPercepts;
    }

    @Override
    public Map<String, Percept> preStep(int stepNo) {
        return null; //TODO
    }

    @Override
    public void step(int stepNo, Map<String, Action> actions) {
        // step job generator
        generator.generateJobs(stepNo).forEach(job -> world.addJob(job));
        // execute all actions in random order
        List<String> agents = world.getAgents();
        RNG.shuffle(agents);
        actionExecutor.preProcess();
        for(String agent: agents){
            actionExecutor.execute(agent, actions);
        }
        actionExecutor.postProcess();
        world.processNewJobs();
    }

    @Override
    public Map<String, Percept> finish() {
        return null; //TODO
    }
}
