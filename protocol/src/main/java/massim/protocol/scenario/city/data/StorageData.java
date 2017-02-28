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

    @XmlElement(name = "stored")
    private List<TeamStoredData> teamItems;

    /**
     * For JAXB
     */
    private StorageData() {
        super();
    }

    /**
     * Constructor. Exactly one of stored/teamStored should be null.
     * @param name name of the storage
     * @param lat latitude
     * @param lon longitude
     * @param capacity max capacity of the storage
     * @param freeSpace unused capacity
     * @param stored items stored for the team receiving this data (may be null)
     * @param teamStored item stored for all teams (may be null)
     */
    public StorageData(String name, double lat, double lon, int capacity, int freeSpace, List<StoredData> stored,
                       List<TeamStoredData> teamStored) {
        super(name, lat, lon);
        totalCapacity = capacity;
        usedCapacity = capacity - freeSpace;
        this.items = stored;
        teamItems = teamStored;
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
     * @return a list of items currently stored/stored-delivered for one team only, may be null
     */
    public List<StoredData> getStoredItems(){
        return items == null? new Vector<>() : items;
    }

    /**
     * @return stored items per team, may be null
     */
    public List<TeamStoredData> getAllStoredItems(){
        return teamItems == null? new Vector<>() : teamItems;
    }

    /**
     * Stores how many items are stored for one team.
     */
    @XmlRootElement(name = "item")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class TeamStoredData{

        @XmlAttribute(name="team")
        public String teamName;

        @XmlElement(name="item")
        public List<StoredData> stored;

        /**
         * For jaxb.
         */
        private TeamStoredData(){}

        /**
         * Constructor.
         * @param team the eam the items are stored for
         * @param teamStored information about how many items of which type are stored
         */
        public TeamStoredData(String team, List<StoredData> teamStored){
            teamName = team;
            stored = teamStored;
        }
    }
}
