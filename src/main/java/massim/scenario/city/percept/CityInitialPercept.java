package massim.scenario.city.percept;

import massim.messages.SimStartContent;
import massim.scenario.city.data.Entity;
import massim.scenario.city.data.WorldState;
import massim.scenario.city.data.jaxb.ItemData;
import massim.scenario.city.data.jaxb.RoleData;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a sim-start percept in the City scenario.
 * @author ta10
 */
@XmlRootElement(name="simulation")
@XmlAccessorType(XmlAccessType.NONE)
public class CityInitialPercept extends SimStartContent {

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

    public CityInitialPercept(String agentName, WorldState world) {
        if(world == null) return;
        simId = world.getSimID();
        steps = world.getSteps();
        teamName = world.getTeamForAgent(agentName);
        map = world.getMapName();
        seedCapital = world.getSeedCapital();
        Entity entity = world.getEntity(agentName);
        this.role = new RoleData(entity.getRole());
        this.items = world.getItems().stream().map(ItemData::new).collect(Collectors.toList());
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
        return items;
    }

    public String getMapName(){
        return map;
    }

    public long getSeedCapital(){
        return seedCapital;
    }

}