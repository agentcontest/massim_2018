package massim.protocol.scenario.city.data;

import massim.protocol.StaticWorldData;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Holds the simulation data which does not change during the simulation.
 */
@XmlRootElement(name="staticCity")
@XmlAccessorType(XmlAccessType.NONE)
public class StaticCityData extends StaticWorldData {

    @XmlAttribute(name="id")
    public String simId;

    @XmlAttribute
    public int steps;

    @XmlAttribute
    public String map;

    @XmlAttribute
    public long seedCapital;

    @XmlList
    public List<String> teams;

    @XmlElement(name="role")
    public List<RoleData> roles;

    @XmlElement(name="item")
    public List<ItemData> items;

    @XmlAttribute
    public double minLat;

    @XmlAttribute
    public double maxLat;

    @XmlAttribute
    public double minLon;

    @XmlAttribute
    public double maxLon;

    /**
     * For jaxb.
     */
    private StaticCityData(){
        super();
    }

    public StaticCityData(String simID, int steps, String map, long seedCapital, List<String> teams,
                          List<RoleData> roles, List<ItemData> items,
                          double minLat, double maxLat, double minLon, double maxLon){
        this.simId = simID;
        this.steps = steps;
        this.map = map;
        this.seedCapital = seedCapital;
        this.teams = teams;
        this.roles = roles;
        this.items = items;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
    }

    public String getSimId() {
        return this.simId;
    }

    public int getSteps() {
        return this.steps;
    }

    public String getMap() {
        return this.map;
    }

    public long getSeedCapital() {
        return this.seedCapital;
    }

    public List<String> getTeams() {
        return this.teams;
    }

    public List<RoleData> getRoles() {
        return this.roles;
    }

    public List<ItemData> getItems() {
        return this.items;
    }

    public double getMinLon() {
        return this.minLon;
    }

    public double getMinLat() {
        return this.minLat;
    }

    public double getMaxLon() {
        return this.maxLon;
    }

    public double getMaxLat() {
        return this.maxLat;
    }
}
