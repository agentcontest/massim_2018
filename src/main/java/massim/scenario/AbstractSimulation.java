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

    private Map<String, Action> lastActions;

    /**
     * Setup the scenario. Called before the first step.
     * @param config the scenario's configuration.
     * @param matchTeams set of participating teams and their details
     */
    public abstract Map<String, Percept> init(JSONObject config, Set<TeamConfig> matchTeams);

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
     * @return mapping from agent names to their percept resulting from the step
     */
    public abstract Map<String, Percept> step(int stepNo);

    /**
     * Finish scenario execution, prepare results, etc.
     */
    public abstract Map<String, Percept> finish();

    /**
     * Stores the actions so they can be processed in the next call to {@link AbstractSimulation#step(int)}
     * @param actions mapping from agent names to actions that were received by the agents
     */
    public void setActions(Map<String,Action> actions) {
        lastActions = actions;
    }

    /**
     * Retrieves the latest action for the given agent.
     * @param agentName name of the agent
     * @return the last action of the agent
     */
    protected Action getAction(String agentName){
        return lastActions.getOrDefault(agentName, Action.NO_ACTION);
    }
}
