package massim.protocol.scenario.city.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds data of a workshop facility.
 */
@XmlRootElement(name = "workshop")
@XmlAccessorType(XmlAccessType.NONE)
public class WorkshopData extends FacilityData {

    /**
     * For JAXB
     */
    private WorkshopData() {}

    /**
     * Constructor.
     * @param name name of the facility
     * @param lat latitude of the facility
     * @param lon longitude of the facility
     */
    public WorkshopData(String name, double lat, double lon) {
        super(name, lat, lon);
    }
}
