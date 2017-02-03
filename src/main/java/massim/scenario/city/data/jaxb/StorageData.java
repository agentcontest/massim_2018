package massim.scenario.city.data.jaxb;

import massim.scenario.city.data.Item;
import massim.scenario.city.data.facilities.Storage;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Vector;

/**
 * Holds JAXB annotated data of a storage facility.
 */
@XmlRootElement(name = "storage")
@XmlAccessorType(XmlAccessType.NONE)
public class StorageData extends FacilityData {

    @XmlAttribute
    private int totalCapacity;

    @XmlAttribute
    private int usedCapacity;

    @XmlElement(name = "item")
    private List<StoredData> items;

    /**
     * For JAXB
     */
    private StorageData() {
        super();
    }

    /**
     * Constructor.
     * @param original the storage to "replicate"
     * @param team the team the information is intended for (items for that team will be stored here)
     * @param allItems all items in the current sim
     */
    public StorageData(Storage original, String team, List<Item> allItems) {
        super(original.getName(), original.getLocation().getLat(), original.getLocation().getLon());
        totalCapacity = original.getCapacity();
        usedCapacity = totalCapacity - original.getFreeSpace();
        allItems.forEach(item -> {
            int stored = original.getStored(item, team);
            int delivered = original.getDelivered(item, team);
            if (stored != 0 || delivered != 0) {
                if(items == null) items = new Vector<>();
                items.add(new StoredData(item.getName(), stored, delivered));
            }
        });
    }

    /**
     * @return the total volume that can be stored in this storage
     */
    public int getTotalCapacity(){
        return totalCapacity;
    }

    /**
     * @return the used up volume of this storage
     */
    public int getUsedCapacity() {
        return usedCapacity;
    }

    /**
     * @return a list of items currently stored/stored-delivered
     */
    public List<StoredData> getStoredItems(){
        return items;
    }
}
