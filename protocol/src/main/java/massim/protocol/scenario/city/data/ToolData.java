package massim.protocol.scenario.city.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds info of a tool for serialization.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ToolData {

    @XmlAttribute
    private String name;

    /*
     * For JAXB
     */
    private ToolData(){}

    /**
     * Constructor.
     * @param name the tool's name
     */
    public ToolData(String name){
        this.name = name;
    }

    /**
     * @return the tool's name
     */
    public String getName(){
        return name;
    }
}
