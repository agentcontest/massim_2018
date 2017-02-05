package massim.scenario.city.percept;

import massim.messages.RequestActionContent;
import massim.scenario.city.data.*;
import massim.scenario.city.data.jaxb.*;

import javax.xml.bind.annotation.*;
import java.util.List;

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
                           List<DumpData> dumps, List<StorageData> storage, List<JobData> jobs){
        simData = new SimData(step);
        teamData = team;
        selfData = new EntityData(world, world.getEntity(agent), true);
        entityData = entities;
        this.shops = shops;
        this.workshops = workshops;
        this.chargingStations = stations;
        this.dumps = dumps;
        this.storage = storage;
        this.jobs = jobs;
    }

    public SimData getSimData() {
        return simData;
    }

    @XmlRootElement(name="simulation")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class SimData{

        @XmlAttribute public int step;

        private SimData(){}
        SimData(int step){
            this.step = step;
        }
    }

}
