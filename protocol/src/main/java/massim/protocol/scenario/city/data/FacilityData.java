package massim.protocol.scenario.city.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created in 2017.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class FacilityData {

    @XmlAttribute
    public String name;

    @XmlAttribute
    private double lat;

    @XmlAttribute
    private double lon;

    /**
     * Constructor for JAXB
     */
    FacilityData(){};

    FacilityData(String name, double lat, double lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * @return the name of the facility
     */
    public String getName(){
        return name == null? "" : name;
    }

    /**
     * @return the latitude of this facility
     */
    public double getLat() {
        return lat;
    }

    /**
     * @return the longitude of this facility
     */
    public double getLon() {
        return lon;
    }
}
