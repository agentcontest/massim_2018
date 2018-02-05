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
    private long massium;

    @XmlAttribute
    private long score;

    /**
     * For JAXB
     */
    private TeamData() {}

    /**
     * @param name the team's name (may be null)
     * @param massium the current massium resource of the team
     * @param score the current score of the team
     */
    public TeamData(String name, long massium, long score) {
        this.name = name;
        this.massium = massium;
        this.score = score;
    }

    /**
     * @return the current money of the team
     */
    public long getMassium() {
        return massium;
    }

    public long getScore() {
        return score;
    }

    /**
     * @return the name of the team or an empty string if that name is not known
     */
    public String getName(){
        return name == null? "": name;
    }
}
