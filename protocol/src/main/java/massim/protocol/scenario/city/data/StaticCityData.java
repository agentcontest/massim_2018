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

    /**
     * For jaxb.
     */
    private StaticCityData(){
        super();
    }

    public StaticCityData(String simID, int steps, String map, long seedCapital, List<String> teams,
                          List<RoleData> roles, List<ItemData> items){
        this.simId = simID;
        this.steps = steps;
        this.map = map;
        this.seedCapital = seedCapital;
        this.teams = teams;
        this.roles = roles;
        this.items = items;
    }
}
