package massim.protocol.scenario.city.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds data of a dump facility.
 */
@XmlRootElement(name = "dump")
@XmlAccessorType(XmlAccessType.NONE)
public class DumpData extends FacilityData {

    /**
     * For JAXB.
     */
    private DumpData() {}

    /**
     * Constructor.
     * @param name name of the facility
     * @param lat latitude of the facility
     * @param lon longitude of the facility
     */
    public DumpData(String name, double lat, double lon) {
        super(name, lat, lon);
    }
}
