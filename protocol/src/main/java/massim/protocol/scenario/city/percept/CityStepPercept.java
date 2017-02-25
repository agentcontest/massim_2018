package massim.protocol.scenario.city.percept;

import massim.protocol.messagecontent.RequestAction;
import massim.protocol.scenario.city.data.*;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * A regular percept that is sent each step in the City scenario.
 */
@XmlRootElement(name="percept")
@XmlAccessorType(XmlAccessType.NONE)
public class CityStepPercept extends RequestAction {

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

    /**
     * Constructor.
     * @param self data of the entity receiving the percept
     * @param teamName name of the entity/agent
     * @param step current step
     * @param team data of the team
     * @param entities data of all entities
     * @param shops all shops
     * @param workshops all workshops
     * @param stations all charging stations
     * @param dumps all dumps
     * @param storage data of all storage facilities containing the items for the team of the agent
     * @param jobsPerTeam map of all jobs by team (some jobs are only seen by a single team)
     */
    public CityStepPercept(EntityData self, String teamName, int step, TeamData team,
                           List<EntityData> entities, List<ShopData> shops, List<WorkshopData> workshops,
                           List<ChargingStationData> stations, List<DumpData> dumps, List<StorageData> storage,
                           Map<String, List<JobData>> jobsPerTeam){
        simData = new SimData(step);
        teamData = team;
        selfData = self;
        entityData = entities;
        this.shops = shops;
        this.workshops = workshops;
        this.chargingStations = stations;
        this.dumps = dumps;
        this.storage = storage;
        this.jobs = jobsPerTeam.get(teamName);
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
        return entityData == null? new Vector<>() : entityData;
    }

    /**
     * @return information about all shops
     */
    public List<ShopData> getShopData(){
        return shops == null? new Vector<>() : shops;
    }

    /**
     * @return information about all workshops
     */
    public List<WorkshopData> getWorkshops(){
        return workshops == null? new Vector<>() : workshops;
    }

    /**
     * @return information about all charging stations
     */
    public List<ChargingStationData> getChargingStations(){
        return chargingStations == null? new Vector<>() : chargingStations;
    }

    /**
     * @return information about all dumps
     */
    public List<DumpData> getDumps(){
        return dumps == null? new Vector<>() : dumps;
    }

    /**
     * @return information about the storage facilities
     */
    public List<StorageData> getStorage(){
        return storage == null? new Vector<>() : storage;
    }

    /**
     * @return information about all jobs
     */
    public List<JobData> getJobs(){
        return jobs == null? new Vector<>() : jobs;
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
