package massim.protocol.scenario.city.data;

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
     * @param name name of the storage
     * @param lat latitude
     * @param lon longitude
     * @param capacity max capacity of the storage
     * @param freeSpace unused capacity
     * @param stored items stored for the team receiving this data
     */
    public StorageData(String name, double lat, double lon, int capacity, int freeSpace, List<StoredData> stored) {
        super(name, lat, lon);
        totalCapacity = capacity;
        usedCapacity = capacity - freeSpace;
        if(stored != null && stored.size() > 0) this.items = stored;
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
        return items == null? new Vector<>() : items;
    }
}
