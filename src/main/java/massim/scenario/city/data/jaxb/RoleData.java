package massim.scenario.city.data.jaxb;

import massim.scenario.city.data.Item;
import massim.scenario.city.data.Role;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Holds data of a role for serialization.
 */
@XmlRootElement(name = "role")
@XmlAccessorType(XmlAccessType.NONE)
public class RoleData {

    @XmlAttribute
    private String name;

    @XmlAttribute
    private int speed;

    @XmlAttribute
    private int battery;

    @XmlAttribute
    private int load;

    @XmlElement(name="tool")
    private List<String> tools;

    /**
     * For JAXB
     */
    private RoleData(){}

    /**
     * Constructor.
     * @param role the role model
     */
    public RoleData(Role role){
        this.name = role.getName();
        this.speed = role.getSpeed();
        this.battery = role.getMaxBattery();
        this.load = role.getMaxLoad();
        this.tools = role.getTools().stream().map(Item::getName).collect(Collectors.toList());
    }

    /**
     * @return the name of the role
     */
    public String getName() {
        return name;
    }

    /**
     * @return the role's speed
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * @return the role's max battery charge
     */
    public int getBattery() {
        return battery;
    }

    /**
     * @return the role's max usable volume
     */
    public int getLoad() {
        return load;
    }

    /**
     * @return all tools usable by the role
     */
    public List<String> getTools() {
        return tools;
    }
}
