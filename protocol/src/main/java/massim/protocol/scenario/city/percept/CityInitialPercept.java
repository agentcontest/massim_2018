package massim.protocol.scenario.city.percept;

import massim.protocol.messagecontent.SimStart;
import massim.protocol.scenario.city.data.ItemData;
import massim.protocol.scenario.city.data.RoleData;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Vector;

/**
 * Represents a sim-start percept in the City scenario.
 * @author ta10
 */
@XmlRootElement(name="simulation")
@XmlAccessorType(XmlAccessType.NONE)
public class CityInitialPercept extends SimStart {

    @XmlAttribute(name="id") private String simId;
    @XmlAttribute private int steps;
    @XmlAttribute private String map;
    @XmlAttribute private long seedCapital;
    @XmlAttribute(name="team") private String teamName;
    @XmlElement private RoleData role;
    @XmlElement(name="item") public List<ItemData> items;

    /**
     * for JAXB
     */
    private CityInitialPercept(){}

    /**
     * Constructor.
     * @param simId id of the simulation
     * @param steps max number of steps
     * @param teamName name of the agent's team
     * @param mapName name of the current map
     * @param seedCapital money to start with
     * @param role role of the entity
     * @param items items available in the simulation
     */
    public CityInitialPercept(String simId, int steps, String teamName, String mapName, long seedCapital,
                              RoleData role, List<ItemData> items) {
        this.simId = simId;
        this.steps = steps;
        this.teamName = teamName;
        this.map = mapName;
        this.seedCapital = seedCapital;
        this.role = role;
        this.items = items;
    }

    public String getId(){
        return simId;
    }

    public int getSteps(){
        return steps;
    }

    public String getTeam(){
        return teamName;
    }

    public RoleData getRoleData(){
        return role;
    }

    public List<ItemData> getItemData(){
        return items == null? new Vector<>() : items;
    }

    public String getMapName(){
        return map;
    }

    public long getSeedCapital(){
        return seedCapital;
    }

}