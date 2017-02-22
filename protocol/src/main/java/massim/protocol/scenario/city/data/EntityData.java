package massim.protocol.scenario.city.data;

import javax.xml.bind.annotation.*;
import java.util.List;

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
    private Integer routeLength;

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
     * @param self whether the data is for the entity itself or for another one
     */
    public EntityData(boolean self, Integer currentBattery, Integer currentLoad, ActionData lastAction,
                      String facilityName, List<WayPointData> route, List<ItemAmountData> items,
                      String agentName, String team, String role, double lat, double lon) {
        if (self) {
            this.charge = currentBattery;
            this.load = currentLoad;
            this.lastAction = lastAction;
            this.facility = facilityName;
            if(route != null){
                if (route.size() > 0 ) {
                    routeLength = route.size();
                    this.route = route;
                }
            }
            this.items = items;

        } else {
            this.team = team;
            this.role = role;
        }
        name = agentName;
        this.lat = lat;
        this.lon = lon;
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
