package massim.protocol.scenario.city.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds data of an item offered in a shop (name, price and amount).
 */
@XmlRootElement(name = "item")
@XmlAccessorType(XmlAccessType.NONE)
public class StockData {

    @XmlAttribute
    private String name;

    @XmlAttribute
    private int price;

    @XmlAttribute
    private int amount;

    /**
     * For JAXB
     */
    private StockData() {}

    /**
     * Constructor.
     * @param itemName name of the item offered
     * @param price price of the item in the shop
     * @param amount amount offered
     */
    public StockData(String itemName, int price, int amount) {
        name = itemName;
        this.price = price;
        this.amount = amount;
    }

    /**
     * @return name of the offered item
     */
    public String getName() {
        return name == null? "" : name;
    }

    /**
     * @return price of the offered item
     */
    public int getPrice() {
        return price;
    }

    /**
     * @return amount offered
     */
    public int getAmount() {
        return amount;
    }
}
