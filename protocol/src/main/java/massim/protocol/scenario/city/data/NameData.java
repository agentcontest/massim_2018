package massim.protocol.scenario.city.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An object with name attribute only.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class NameData {

    @XmlAttribute
    private String name;

    /**
     * For JAXB
     */
    private NameData(){}

    /**
     * Constructor.
     * @param name name of the item
     */
    public NameData(String name){
        this.name = name;
    }

    /**
     * @return the item's name
     */
    public String getName() {
        return name == null? "" : name;
    }
}