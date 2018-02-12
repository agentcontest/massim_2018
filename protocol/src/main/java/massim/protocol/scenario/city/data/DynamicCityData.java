package massim.protocol.scenario.city.data;

import massim.protocol.DynamicWorldData;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Holds the world data that may change in each step.
 */
@XmlRootElement(name="dynamicCity")
@XmlAccessorType(XmlAccessType.NONE)
public class DynamicCityData extends DynamicWorldData {

    @XmlElement(name="entity")
    public List<EntityData> entities;

    @XmlElement(name="shop")
    public List<ShopData> shops;

    @XmlElement(name="workshop")
    public List<WorkshopData> workshops;

    @XmlElement(name="chargingStation")
    public List<ChargingStationData> chargingStations;

    @XmlElement(name="dump")
    public List<DumpData> dumps;

    @XmlElement(name="job")
    public List<JobData> jobs;

    @XmlElement(name="storage")
    public List<StorageData> storages;

    @XmlElement(name="resourceNode")
    public List<ResourceNodeData> resourceNodes;

    @XmlElement(name="team")
    public List<TeamData> teams;

    @XmlElement(name="well")
    public List<WellData> wells;

    /**
     * For jaxb
     */
    private DynamicCityData(){
        super();
    }

    public DynamicCityData(int step, List<EntityData> entities, List<ShopData> shops, List<WorkshopData> workshops,
                           List<ChargingStationData> stations, List<DumpData> dumps, List<ResourceNodeData> resourceNodes, List<JobData> jobs,
                           List<StorageData> storages, List<WellData> wells, List<TeamData> teams){
        super(step);
        this.entities = entities;
        this.shops = shops;
        this.workshops = workshops;
        this.chargingStations = stations;
        this.dumps = dumps;
        this.jobs = jobs;
        this.storages = storages;
        this.resourceNodes = resourceNodes;
        this.teams = teams;
        this.wells = wells;
    }

    public List<WorkshopData> getWorkshops() {
        return this.workshops;
    }

    public List<ChargingStationData> getChargingStations() {
        return this.chargingStations;
    }

    public List<ShopData> getShops() {
        return this.shops;
    }

    public List<DumpData> getDumps() {
        return this.dumps;
    }

    public List<ResourceNodeData> getResourceNodes() {
        return this.resourceNodes;
    }

    public List<StorageData> getStorages() {
        return this.storages;
    }

    public List<EntityData> getEntities() {
        return this.entities;
    }

    public List<JobData> getJobs() {
        return this.jobs;
    }

    public List<TeamData> getTeams() {
        return this.teams;
    }

    public List<WellData> getWells() {
        return wells;
    }
}
