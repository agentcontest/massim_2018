package massim.scenario.city.data.facilities;

import massim.scenario.city.data.Location;
import massim.scenario.city.data.Item;
import massim.util.Log;

/**
 * Created by Sarah on 07.03.2017.
 */
public class ResourceNode extends Facility{

    private final Item resource;
    private final int threshold;
    private int gathered = 0;

    /**
     * Creates a new resource node.
     * @param name the name of the resource node.
     * @param location the resource node's location.
     * @param resource the resource that is available in this resource node
     * @param threshold the threshold that needs to be achieved (using skill) to mine 1 resource
     */
    public ResourceNode(String name, Location location, Item resource, int threshold){
        super(name, location);
        if(resource.needsAssembly()) Log.log(Log.Level.ERROR, "Resource node should not yield assembled items.");
        this.resource = resource;
        this.threshold = threshold;
    }

    /**
     *
     * @return the resource that is available at this resource node
     */
    public Item getResource() { return resource; }

    /**
     * Tries to get a resource from this node.
     * @param skill the skill of the gathering agent
     * @return the number of gathered resources (0 - x)
     */
    public int gather(int skill){
        gathered += skill;
        int result = gathered / threshold;
        gathered %= threshold;
        return result;
    }

    public int getThreshold() {
        return threshold;
    }
}