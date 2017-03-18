package massim.scenario.city.data.facilities;

import massim.scenario.city.data.Location;
import massim.scenario.city.data.Item;

/**
 * Created by Sarah on 07.03.2017.
 */
public class ResourceNode extends Facility{

    private Item resource;

    /**
     * Creates a new resource node.
     * @param name the name of the resource node.
     * @param location the resource node's location.
     * @param resource the resource that is available in this resource node.
     */
    public ResourceNode(String name, Location location, Item resource){
        super(name, location);
        this.resource = resource;
    }

    public Item getResource() { return resource; }

}
