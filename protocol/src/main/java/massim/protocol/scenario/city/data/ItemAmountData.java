package massim.protocol.scenario.city.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Stores an amount for an item, e.g. to store crafting requirements.
 */
@XmlRootElement(name="item")
@XmlAccessorType(XmlAccessType.NONE)
public class ItemAmountData {

    @XmlAttribute
    private String name;

    @XmlAttribute
    private int amount;

    /**
     * For JAXB
     */
    private ItemAmountData(){}

    /**
     * Constructor.
     * @param name name of the item
     * @param amount how many items to represent
     */
    public ItemAmountData(String name, int amount){
        this.name = name;
        this.amount = amount;
    }

    /**
     * @return the item's name
     */
    public String getName() {
        return name == null? "" : name;
    }

    /**
     * @return the item amount
     */
    public int getAmount() {
        return amount;
    }
}