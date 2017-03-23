package massim.protocol.scenario.city.percept;

import massim.protocol.messagecontent.RequestAction;
import massim.protocol.scenario.city.data.*;
import massim.protocol.scenario.city.util.LocationUtil;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

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
    @XmlElement(name="resourceNode") private List<ResourceNodeData> resourceNodes;
    @XmlElement(name="job") private List<JobData> jobs;
    @XmlElement(name="auction") private List<AuctionJobData> auctions;
    @XmlElement(name="mission") private List<MissionData> missions;
    @XmlElement(name="posted") private List<JobData> postedJobs;

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
     * @param resourceNodes all resource nodes
     * @param jobsPerTeam map of all jobs by team (not including jobs posted by the team per entry)
     * @param auctionsPerTeam map of all auctions by team (assigned auctions are only visible to the assigned team)
     * @param missionsPerTeam mission jobs for each team
     * @param postedJobsPerTeam map of all posted jobs by team
     * @param visRange the visibility range for determining whether to include certain elements
     */
    public CityStepPercept(EntityData self, String teamName, int step, TeamData team,
                           List<EntityData> entities,
                           List<ShopData> shops,
                           List<WorkshopData> workshops,
                           List<ChargingStationData> stations,
                           List<DumpData> dumps,
                           List<StorageData> storage,
                           List<ResourceNodeData> resourceNodes,
                           Map<String, List<JobData>> jobsPerTeam,
                           Map<String, List<AuctionJobData>> auctionsPerTeam,
                           Map<String, List<MissionData>> missionsPerTeam,
                           Map<String, List<JobData>> postedJobsPerTeam,
                           int visRange){
        simData = new SimData(step);
        teamData = team;
        selfData = self;
        entityData = entities;
        this.shops = shops;
        this.workshops = workshops;
        this.chargingStations = stations;
        this.dumps = dumps;
        this.storage = storage;
        this.resourceNodes = resourceNodes.stream() // filter nodes by visibility range
                .filter(rn -> LocationUtil.calculateRange(rn.getLat(), rn.getLon(), self.getLat(), self.getLon()) <= visRange)
                .collect(Collectors.toList());
        this.jobs = jobsPerTeam.get(teamName);
        this.auctions = auctionsPerTeam.get(teamName);
        this.postedJobs = postedJobsPerTeam.get(teamName);
        this.missions = missionsPerTeam.get(teamName);
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
     * @return information about the resource nodes
     */
    public List<ResourceNodeData> getResourceNodes(){
        return resourceNodes == null? new Vector<>() : resourceNodes;
    }

    /**
     * @return information about all non-auction jobs
     */
    public List<JobData> getJobs(){
        return jobs == null? new Vector<>() : jobs;
    }

    /**
     * @return information about all auctions
     */
    public List<AuctionJobData> getAuctions(){
        return auctions == null? new Vector<>() : auctions;
    }

    /**
     * @return information about all jobs posted by the team
     */
    public List<JobData> getPostedJobs(){
        return postedJobs == null? new Vector<>() : postedJobs;
    }

    /**
     * @return information about all jobs posted by the team
     */
    public List<MissionData> getMissions(){
        return missions == null? new Vector<>() : missions;
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
