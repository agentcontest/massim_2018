package massim.scenario.city.percept;

import massim.messages.RequestActionContent;
import massim.scenario.city.data.WorldState;
import massim.scenario.city.data.jaxb.*;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * A regular percept that is sent each step in the City scenario.
 */
@XmlRootElement(name="percept")
@XmlAccessorType(XmlAccessType.NONE)
public class CityStepPercept extends RequestActionContent {

    // ID and deadline are inherited

    @XmlElement(name="simulation") private SimData simData;
    @XmlElement(name="self") private EntityData selfData;
    @XmlElement(name="team") private TeamData teamData;
    @XmlElement(name="entity") private List<EntityData> entityData;
    @XmlElement(name="shop") private List<ShopData> shops;
    @XmlElement(name="workshop") private List<WorkshopData> workshops;
    @XmlElement(name="chargingStation") private List<ChargingStationData> chargingStations;
    @XmlElement(name="dump") private List<DumpData> dumps;
    @XmlElement(name="storage") private List<StorageData> storage;
    @XmlElement(name="job") private List<JobData> jobs;

    private CityStepPercept(){} // for jaxb
    public CityStepPercept(String agent, WorldState world, int step, TeamData team, List<EntityData> entities,
                           List<ShopData> shops, List<WorkshopData> workshops, List<ChargingStationData> stations,
                           List<DumpData> dumps, List<StorageData> storage, Map<String, List<JobData>> jobsPerTeam){
        simData = new SimData(step);
        teamData = team;
        selfData = new EntityData(world, world.getEntity(agent), true);
        entityData = entities;
        this.shops = shops;
        this.workshops = workshops;
        this.chargingStations = stations;
        this.dumps = dumps;
        this.storage = storage;
        this.jobs = jobsPerTeam.get(world.getTeamForAgent(agent));
    }

    /**
     * @return information about the entity receiving the percept
     */
    public EntityData getSelfData(){
        return selfData;
    }

    /**
     * @return information about the entity's team
     */
    public TeamData getTeamData(){
        return teamData;
    }

    /**
     * @return information about all entities in the simulation
     */
    public List<EntityData> getEntityData(){
        return entityData;
    }

    /**
     * @return information about all shops
     */
    public List<ShopData> getShopData(){
        return shops;
    }

    /**
     * @return information about all workshops
     */
    public List<WorkshopData> getWorkshops(){
        return workshops;
    }

    /**
     * @return information about all charging stations
     */
    public List<ChargingStationData> getChargingStations(){
        return chargingStations;
    }

    /**
     * @return information about all dumps
     */
    public List<DumpData> getDumps(){
        return dumps;
    }

    /**
     * @return information about the storage facilities
     */
    public List<StorageData> getStorage(){
        return storage;
    }

    /**
     * @return information about all jobs
     */
    public List<JobData> getJobs(){
        return jobs;
    }

    /**
     * @return the sim data object containing info about the simulation
     */
    public SimData getSimData() {
        return simData;
    }

    @XmlRootElement(name="simulation")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class SimData{

        @XmlAttribute private int step;

        /**
         * For jaxb
         */
        private SimData(){}

        /**
         * Constructor.
         * @param step the current step
         */
        SimData(int step){
            this.step = step;
        }

        /**
         * @return the current step
         */
        public int getStep(){
            return step;
        }
    }

}
