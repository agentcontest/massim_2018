package massim.protocol.scenario.city.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds data of a waypoint in a route
 */
@XmlRootElement(name = "n")
@XmlAccessorType(XmlAccessType.NONE)
public class WayPointData {

    @XmlAttribute(name = "i")
    private int index;

    @XmlAttribute
    private double lat;

    @XmlAttribute
    private double lon;

    /**
     * For JAXB
     */
    private WayPointData() {}

    /**
     * Constructor.
     * @param index index within a route
     * @param lat latitude of the point
     * @param lon longitude of the point
     */
    public WayPointData(int index, double lat, double lon) {
        this.index = index;
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * @return the point's index in a route
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return latitude of the point
     */
    public double getLat() {
        return lat;
    }

    /**
     * @return longitude of the point
     */
    public double getLon() {
        return lon;
    }
}
