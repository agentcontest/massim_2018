package massim.protocol.scenario.city.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds data of a charging station facility.
 */
@XmlRootElement(name = "resourceNode")
@XmlAccessorType(XmlAccessType.NONE)
public class ResourceNodeData extends FacilityData {

    @XmlAttribute
    private String resource;

    /**
     * For JAXB
     */
    private ResourceNodeData() {}

    /**
     * Constructor.
     * @param name name of the facility
     * @param lat latitude of the facility
     * @param lon longitude of the facility
     //* @param gatherFrequency number of actions until the next resource becomes available
     */
    public ResourceNodeData(String name, double lat, double lon, String resource) {
        super(name, lat, lon);
        this.resource = resource;
    }

   /**
     * @return the nodes's resource
     */
    public String getResource() {
        return resource;
    }

}
