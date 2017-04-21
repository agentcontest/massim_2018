package massim.protocol.scenario.city.data;



import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Vector;
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
    private List<ToolData> tools;

    /**
     * For JAXB
     */
    private RoleData(){}

    /**
     * Constructor.
     * @param name name of the role
     * @param speed speed of the role
     * @param maxBattery maximum charge
     * @param maxLoad max capacity
     * @param tools tools usable by the role
     */
    public RoleData(String name, int speed, int maxBattery, int maxLoad, List<ToolData> tools){
        this.name = name;
        this.speed = speed;
        this.battery = maxBattery;
        this.load = maxLoad;
        this.tools = tools;
    }

    /**
     * @return the name of the role
     */
    public String getName() {
        return name == null? "" : name;
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
        return tools == null? new Vector<>() : tools.stream()
                                                    .map(ToolData::getName)
                                                    .collect(Collectors.toList());
    }

    /**
     * Adds a tool to this role.
     * @param tool name of the tool
     */
    public void addTool(String tool) {
        tools.add(new ToolData(tool));
    }
}
