package massim.protocol.scenario.city.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds data for an item stored (in a storage).
 */
@XmlRootElement(name = "item")
@XmlAccessorType(XmlAccessType.NONE)
public class StoredData {

    @XmlAttribute
    private String name;

    @XmlAttribute
    private int stored;

    @XmlAttribute
    private int delivered;

    /**
     * For jaxb
     */
    private StoredData() {}

    /**
     * Constructor.
     * @param item name of an item
     * @param stored amount stored
     * @param delivered amount stored in the "delivered" section
     */
    public StoredData(String item, int stored, int delivered) {
        this.name = item;
        this.stored = stored;
        this.delivered = delivered;
    }

    /**
     * @return the name of the item
     */
    public String getName() {
        return name == null? "" : name;
    }

    /**
     * @return the amount stored
     */
    public int getStored() {
        return stored;
    }

    /**
     * @return the stored amount in the "delivered" section
     */
    public int getDelivered(){
        return delivered;
    }
}
