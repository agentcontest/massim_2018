package massim.scenario.city.percept;

import massim.messages.SimStartPercept;
import massim.scenario.city.data.Entity;
import massim.scenario.city.data.Item;
import massim.scenario.city.data.Role;
import massim.scenario.city.data.WorldState;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Represents a sim-start percept in the City scenario.
 * @author ta10
 */
@XmlRootElement(name="simulation")
@XmlAccessorType(XmlAccessType.NONE)
public class CityInitialPercept extends SimStartPercept {

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

    /**
     * Info of a role for serialization.
     */
    @XmlRootElement(name = "role")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class RoleData {
        @XmlAttribute public String name;
        @XmlAttribute int speed;
        @XmlAttribute int battery;
        @XmlAttribute int load;
        @XmlElement(name="tool") List<String> tools;
        private RoleData(){} //used by jaxb
        public RoleData(Role role){
            this.name = role.getName();
            this.speed = role.getSpeed();
            this.battery = role.getMaxBattery();
            this.load = role.getMaxLoad();
            this.tools = role.getTools().stream().map(Item::getName).collect(Collectors.toList());
        }
    }

    /**
     * Complete info of an item for serialization.
     */
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class ItemData {
        @XmlAttribute public String name;
        @XmlAttribute public int volume;
        @XmlElement(name="item") public List<ItemAmountData> parts;
        @XmlElement(name="tool") public List<String> tools;

        private ItemData(){} //for jaxb
        public ItemData(Item original){
            this.name = original.getName();
            this.volume = original.getVolume();
            if (original.getRequiredItems().size() > 0) parts = new Vector<>();
            for (Map.Entry<Item, Integer> entry : original.getRequiredItems().entrySet()) {
                parts.add(new ItemAmountData(entry.getKey().getName(), entry.getValue()));
            }
            tools = original.getRequiredTools().stream().map(Item::getName).collect(Collectors.toList());
        }
    }

    /**
     * Complete info of an item for serialization.
     */
    @XmlRootElement(name="item")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class ItemAmountData {
        @XmlAttribute public String name;
        @XmlAttribute public int amount;
        private ItemAmountData(){}
        public ItemAmountData(String name, int amount){
            this.name = name;
            this.amount = amount;
        }
    }
}