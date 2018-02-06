package massim.protocol.scenario.city.data;



import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds data of a role for serialization.
 */
@XmlRootElement(name = "role")
@XmlAccessorType(XmlAccessType.NONE)
public class RoleData {

    @XmlAttribute
    private String name;

    @XmlAttribute
    private int baseSpeed;

    @XmlAttribute
    private int maxSpeed;

    @XmlAttribute
    private int baseBattery;

    @XmlAttribute
    private int maxBattery;

    @XmlAttribute
    private int baseLoad;

    @XmlAttribute
    private int maxLoad;

    @XmlAttribute
    private int baseSkill;

    @XmlAttribute
    private int maxSkill;

    @XmlAttribute
    private int baseVision;

    @XmlAttribute
    private int maxVision;

    /**
     * For JAXB
     */
    private RoleData(){}

    public RoleData(String name, int baseSpeed, int maxSpeed, int baseBattery, int maxBattery, int baseLoad,
                    int maxLoad, int baseSkill, int maxSkill, int baseVision, int maxVision) {
        this.name = name;
        this.baseSpeed = baseSpeed;
        this.maxSpeed = maxSpeed;
        this.baseBattery = baseBattery;
        this.maxBattery = maxBattery;
        this.baseLoad = baseLoad;
        this.maxLoad = maxLoad;
        this.baseSkill = baseSkill;
        this.maxSkill = maxSkill;
        this.baseVision = baseVision;
        this.maxVision = maxVision;
    }

    public String getName() {
        return name;
    }

    public int getBaseSpeed() {
        return baseSpeed;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public int getBaseBattery() {
        return baseBattery;
    }

    public int getMaxBattery() {
        return maxBattery;
    }

    public int getBaseLoad() {
        return baseLoad;
    }

    public int getMaxLoad() {
        return maxLoad;
    }

    public int getBaseSkill() {
        return baseSkill;
    }

    public int getMaxSkill() {
        return maxSkill;
    }

    public int getBaseVision() {
        return baseVision;
    }

    public int getMaxVision() {
        return maxVision;
    }
}
