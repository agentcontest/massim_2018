package massim.scenario;

import massim.Action;
import massim.config.TeamConfig;
import massim.messages.SimEndContent;
import massim.messages.SimStartPercept;
import massim.messages.StepPercept;
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
     * @return map of agent names to their respective initial (sim-start) percept
     */
    public abstract Map<String, SimStartPercept> init(int steps, JSONObject config, Set<TeamConfig> matchTeams);

    /**
     * Called before each step.
     * Can be used to prepare the actual step and definitely needs to calculate the agents' percepts.
     * @param stepNo number of the simulation step
     * @return map from agent names to their current step percept (for request-action message)
     */
    public abstract Map<String, StepPercept> preStep(int stepNo);

    /**
     * Execute one step in the scenario.
     * The agent's new actions have been set now.
     * @param stepNo number of the simulation step
     * @param actionMap mapping from agent names to their actions for this step
     */
    public abstract void step(int stepNo, Map<String, Action> actionMap);

    /**
     * Finish scenario execution, prepare results, etc.
     * @return map from agent names to sim-end percepts
     */
    public abstract Map<String, SimEndContent> finish();
}
