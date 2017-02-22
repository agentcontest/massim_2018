package massim.protocol.scenario.city.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds data of a charging station facility.
 */
@XmlRootElement(name = "charging")
@XmlAccessorType(XmlAccessType.NONE)
public class ChargingStationData extends FacilityData {

    @XmlAttribute
    private int rate;

    /**
     * For JAXB
     */
    private ChargingStationData() {}

    /**
     * Constructor.
     * @param name name of the facility
     * @param lat latitude of the facility
     * @param lon longitude of the facility
     * @param rate charging rate of the station
     */
    public ChargingStationData(String name, double lat, double lon, int rate) {
        super(name, lat, lon);
        this.rate = rate;
    }

    /**
     * @return the station's charging rate
     */
    public int getRate() {
        return rate;
    }
}