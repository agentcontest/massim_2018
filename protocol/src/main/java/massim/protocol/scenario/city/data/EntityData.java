package massim.protocol.scenario.city.data;

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
    private Integer chargeMax;

    @XmlAttribute
    private Integer load;

    @XmlAttribute
    private Integer loadMax;

    @XmlAttribute
    private Integer vision;

    @XmlAttribute
    private Integer skill;

    @XmlAttribute
    private Integer speed;

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
     * Constructor. Make sure to only give the parameters you want serialized.
     * @param currentBattery current charge
     * @param maxBattery the entity's battery capacity
     * @param currentLoad currently used capacity
     * @param maxLoad the entity's maximum capacity
     * @param vision the current vision
     * @param speed the current speed of the entity
     * @param lastAction last action used
     * @param facilityName facility the agent currently is in, if so
     * @param route current route of the agent if available.
     *              only included if it contains at least one waypoint.
     * @param items items currently carried
     * @param agentName name of the agent
     * @param team name of the agent's team
     * @param role role of the agent
     * @param lat latitude
     * @param lon longitude
     */
    public EntityData(Integer currentBattery, Integer maxBattery, Integer currentLoad, Integer maxLoad,
                      Integer vision, Integer skill, Integer speed,
                      ActionData lastAction,
                      String facilityName, List<WayPointData> route, List<ItemAmountData> items,
                      String agentName, String team, String role, double lat, double lon) {
        this.charge = currentBattery;
        this.chargeMax = maxBattery;
        this.load = currentLoad;
        this.loadMax = maxLoad;
        this.lastAction = lastAction;
        this.facility = facilityName;
        this.vision = vision;
        this.skill = skill;
        this.speed = speed;
        if(route != null){
            if (route.size() > 0 ) {
                routeLength = route.size();
                this.route = route;
            }
        }
        this.items = items;
        this.team = team;
        this.role = role;
        this.name = agentName;
        this.lat = lat;
        this.lon = lon;
    }

    public String getName() {
        return name == null? "" : name;
    }

    public Integer getCharge() {
        return charge == null? 0 : charge;
    }

    public Integer getChargeMax() {
        return chargeMax == null? 0 : chargeMax;
    }

    public Integer getLoad() {
        return load == null? 0 : load;
    }

    public Integer getLoadMax() {
        return loadMax == null? 0 : loadMax;
    }

    public ActionData getLastAction() {
        return lastAction;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getFacility() {
        return facility == null? "" : facility;
    }

    public Integer getVision() {
        return vision == null? 0 : vision;
    }

    public Integer getSkill() {
        return skill == null? 0 : skill;
    }

    public Integer getSpeed() {
        return speed == null? 0 : speed;
    }

    public Integer getRouteLength() {
        return routeLength == null? 0: routeLength;
    }

    public String getTeam() {
        return team == null? "" : team;
    }

    public String getRole() {
        return role == null? "" : role;
    }

    public List<ItemAmountData> getItems() {
        return items == null? new Vector<>() : items;
    }

    public List<WayPointData> getRoute() {
        return route == null? new Vector<>() : route;
    }
}
