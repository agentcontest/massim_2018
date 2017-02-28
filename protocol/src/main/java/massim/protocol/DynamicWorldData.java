package massim.protocol;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds all the world data that may change in each step.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class DynamicWorldData extends WorldData {

    @XmlAttribute
    public int step;

    /**
     * For jaxb
     */
    protected DynamicWorldData(){}

    public DynamicWorldData(int step){
        this.step = step;
    }
}
