package massim.protocol.scenario.city.data;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Vector;

/**
 * Holds data of a shop facility.
 */
@XmlRootElement(name = "shop")
@XmlAccessorType(XmlAccessType.NONE)
public class ShopData extends FacilityData {

    @XmlElement(name = "item")
    private List<StockData> stocks = new Vector<>();

    /**
     * For jaxb
     */
    private ShopData() {}

    /**
     * Constructor.
     * @param name name of the shop
     * @param lat latitude
     * @param lon longitude
     */
    public ShopData(String name, double lat, double lon) {
        super(name, lat, lon);
    }
}
