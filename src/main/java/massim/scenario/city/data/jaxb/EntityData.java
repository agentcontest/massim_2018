package massim.scenario.city.data.jaxb;

import massim.scenario.city.data.*;
import massim.scenario.city.data.facilities.Facility;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Vector;

/**
 * Holds the data of an entity.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EntityData {

    @XmlAttribute
    private String name;

    @XmlAttribute
    private Integer charge;

    @XmlAttribute
    private Integer load;

    @XmlElement(name = "action")
    private ActionData lastAction;

    @XmlAttribute
    private double lat;

    @XmlAttribute
    private double lon;

    @XmlAttribute
    private String facility;

    @XmlAttribute
    private int routeLength;

    @XmlAttribute
    private String team;

    @XmlAttribute
    private String role;

    @XmlElement
    private List<ItemAmountData> items;

    @XmlElement
    private List<WayPointData> route;

    /**
     * For JAXB
     */
    private EntityData() {}

    /**
     * Constructor
     * @param world the current world state
     * @param original the entity to extract information from
     * @param self whether the data is for the entity itself or for another one
     */
    public EntityData(WorldState world, Entity original, boolean self) {
        if (self) {
            charge = original.getCurrentBattery();
            load = original.getCurrentLoad();
            lastAction = new ActionData(original.getLastAction(), original.getLastActionResult());
            Facility fac = world.getFacilityByLocation(original.getLocation());
            if (fac != null) facility = fac.getName();
            Route route = original.getRoute();
            routeLength = route == null ? 0 : route.getWaypoints().size();
            ItemBox box = original.getInventory();
            if (box.getStoredTypes().size() > 0) items = new Vector<>();
            box.getStoredTypes().forEach(item -> items.add(new ItemAmountData(item.getName(), box.getItemCount(item))));
            if (route != null) {
                this.route = new Vector<>();
                int i = 0;
                for (Location loc : route.getWaypoints()) {
                    this.route.add(new WayPointData(i++, loc.getLat(), loc.getLon()));
                }
            }
        } else {
            team = world.getTeamForAgent(world.getAgentForEntity(original));
            role = original.getRole().getName();
        }
        name = world.getAgentForEntity(original);
        lat = original.getLocation().getLat();
        lon = original.getLocation().getLon();
    }

    /**
     * @return the name of the entity
     */
    public String getName() {
        return name;
    }

    /**
     * @return the battery charge of the entity
     */
    public Integer getCharge() {
        return charge;
    }

    /**
     * @return the currently used up volume of the entity
     */
    public Integer getLoad() {
        return load;
    }

    /**
     * @return the action that was last executed by the entity
     */
    public ActionData getLastAction() {
        return lastAction;
    }

    /**
     * @return the latitude of the entity
     */
    public double getLat() {
        return lat;
    }

    /**
     * @return the longitude of the entity
     */
    public double getLon() {
        return lon;
    }

    /**
     * @return the facility the entity currently is in (or null)
     */
    public String getFacility() {
        return facility;
    }

    /**
     * @return the length of the entity's current route
     */
    public Integer getRouteLength() {
        return routeLength;
    }

    /**
     * @return name of the entity's team
     */
    public String getTeam() {
        return team;
    }

    /**
     * @return the entity's role
     */
    public String getRole() {
        return role;
    }

    /**
     * @return list of items the entity is carrying
     */
    public List<ItemAmountData> getItems() {
        return items;
    }

    /**
     * @return if the entity has a route, the waypoints of that route, else null
     */
    public List<WayPointData> getRoute() {
        return route;
    }
}
