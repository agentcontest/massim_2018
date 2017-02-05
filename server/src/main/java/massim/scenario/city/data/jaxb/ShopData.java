package massim.scenario.city.data.jaxb;

import massim.scenario.city.data.facilities.Shop;

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
     * @param original the shop to take the data from
     */
    public ShopData(Shop original) {
        super(original.getName(), original.getLocation().getLat(), original.getLocation().getLon());
        restock = original.getRestock();
        original.getOfferedItems().forEach(item ->
                stocks.add(new StockData(item.getName(), original.getPrice(item), original.getItemCount(item))));
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
