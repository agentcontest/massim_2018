package massim.protocol.scenario.city.data;

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
    private String name;

    @XmlAttribute
    private long money;

    /**
     * For JAXB
     */
    private TeamData() {}

    /**
     * @param name the team's name (may be null)
     * @param money the current money of the team
     */
    public TeamData(String name, long money) {
        this.name = name;
        this.money = money;
    }

    /**
     * @return the current money of the team
     */
    public long getMoney() {
        return money;
    }

    /**
     * @return the name of the team or an empty string if that name is not known
     */
    public String getName(){
        return name == null? "": name;
    }
}
