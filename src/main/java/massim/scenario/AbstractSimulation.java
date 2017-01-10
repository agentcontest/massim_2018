package massim.scenario;

import org.json.JSONObject;

/**
 * A (very abstract) simulation.
 * @author ta10
 */
public abstract class AbstractSimulation {

    /**
     * @return the unique ID of the scenario
     */
    public abstract String getID();

    /**
     * Setup the scenario. Called before the first step.
     * @param config the scenario's configuration.
     */
    public abstract void init(JSONObject config);

    /**
     * Called before each step.
     * @param stepNo number of the simulation step
     */
    public abstract void preStep(int stepNo);

    /**
     * Execute one step in the scenario.
     * @param stepNo number of the simulation step
     */
    public abstract void step(int stepNo);

    /**
     * Called after each step.
     * @param stepNo number of the simulation step
     */
    public abstract void postStep(int stepNo);

    /**
     * Finish scenario execution, prepare results, etc.
     */
    public abstract void finish();
}
