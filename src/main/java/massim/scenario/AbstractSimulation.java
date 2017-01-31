package massim.scenario;

import massim.Action;
import massim.Percept;
import massim.config.TeamConfig;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

/**
 * A (very abstract) simulation.
 * @author ta10
 */
public abstract class AbstractSimulation {

    /**
     * Setup the scenario. Called before the first step.
     * @param steps total number of steps
     * @param config the scenario's configuration.
     * @param matchTeams set of participating teams and their details
     */
    public abstract Map<String, Percept> init(int steps, JSONObject config, Set<TeamConfig> matchTeams);

    /**
     * Called before each step.
     * Can be used to prepare the actual step and definitely needs to calculate the agents' percepts.
     * The agent's actions are (of course) not set yet as they need the percepts first.
     * @param stepNo number of the simulation step
     */
    public abstract Map<String, Percept> preStep(int stepNo);

    /**
     * Execute one step in the scenario.
     * The agent's new actions have been set now.
     * @param stepNo number of the simulation step
     * @param actionMap mapping from agent names to their actions for this step
     */
    public abstract void step(int stepNo, Map<String, Action> actionMap);

    /**
     * Finish scenario execution, prepare results, etc.
     */
    public abstract Map<String, Percept> finish();
}
