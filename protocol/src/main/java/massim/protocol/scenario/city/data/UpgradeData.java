package massim.protocol.scenario.city.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds information about an upgrade.
 */
@XmlRootElement(name = "upgrade")
@XmlAccessorType(XmlAccessType.NONE)
public class UpgradeData {

    @XmlAttribute
    private String name;

    @XmlAttribute
    private int cost;

    @XmlAttribute
    private int step;

    // jaxb
    private UpgradeData(){}

    public UpgradeData(String name, int cost, int step) {
        this.name = name;
        this.cost = cost;
        this.step = step;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public int getStep() {
        return step;
    }
}
