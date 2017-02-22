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

    @XmlAttribute
    private int restock;

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
     * @param restock shop's restock interval
     * @param items items offered in the shop (may be null or empty)
     */
    public ShopData(String name, double lat, double lon, int restock, List<StockData> items) {
        super(name, lat, lon);
        this.restock = restock;
        if(items != null && items.size() > 0) stocks = items;
    }

    /**
     * @return the restock interval of the shop
     */
    public int getRestock() {
        return restock;
    }

    /**
     * @return a list of the shop's offerings
     */
    public List<StockData> getOfferedItems(){
        return stocks;
    }
}
