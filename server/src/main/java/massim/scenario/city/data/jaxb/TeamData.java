package massim.scenario.city.data.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds data of a team.
 */
@XmlRootElement(name = "team")
@XmlAccessorType(XmlAccessType.NONE)
public class TeamData {

    @XmlAttribute
    private long money;

    /**
     * For JAXB
     */
    private TeamData() {}

    /**
     * @param money the current money of the team
     */
    public TeamData(long money) {
        this.money = money;
    }

    /**
     * @return the current money of the team
     */
    public long getMoney() {
        return money;
    }
}
