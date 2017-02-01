package massim.scenario.city.percept;

import massim.Action;
import massim.messages.StepPercept;
import massim.scenario.city.data.*;
import massim.scenario.city.data.facilities.Facility;
import massim.scenario.city.data.facilities.Shop;
import massim.scenario.city.data.facilities.Storage;

import javax.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * A regular percept that is sent each step in the City scenario.
 */
@XmlRootElement(name="percept")
@XmlAccessorType(XmlAccessType.NONE)
public class CityStepPercept extends StepPercept {

    // ID and deadline are inherited

    @XmlElement
    private SimData simData;

    @XmlElement(name="self")
    private EntityData selfData;

    @XmlElement
    private TeamData teamData;

    @XmlElement(name="entities")
    private List<EntityData> entityData;

    @XmlElement
    private List<ShopData> shops;

    @XmlElement
    private List<WorkshopData> workshops;

    @XmlElement
    private List<ChargingStationData> chargingStations;

    @XmlElement
    private List<DumpData> dumps;

    @XmlElement
    private List<StorageData> storage;

    public CityStepPercept(String agent, WorldState world, int step, TeamData team, List<EntityData> entities,
                           List<ShopData> shops, List<WorkshopData> workshops, List<ChargingStationData> stations,
                           List<DumpData> dumps, List<StorageData> storage){
        simData = new SimData(step);
        teamData = team;
        selfData = new EntityData(world, world.getEntity(agent), true);
        entityData = entities;
        this.shops = shops;
        this.workshops = workshops;
        this.chargingStations = stations;
        this.dumps = dumps;
        this.storage = storage;
    }

    public SimData getSimData() {
        return simData;
    }

    @XmlRootElement(name="simulation")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class SimData{
        @XmlAttribute
        public int step;

        private SimData(){}
        SimData(int step){
            this.step = step;
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class EntityData{

        @XmlAttribute
        public String name;

        @XmlAttribute
        public int charge;

        @XmlAttribute
        public int load;

        @XmlElement(name="action")
        public ActionData lastAction;

        @XmlAttribute
        public double lat;

        @XmlAttribute
        public double lon;

        @XmlAttribute
        public String facility;

        @XmlAttribute
        public int routeLength;

        @XmlAttribute
        public String team;

        @XmlAttribute
        public String role;

        @XmlElementWrapper //TODO Maps und Listen testen und anpassen
        Map<String, Integer> items = new HashMap<>();

        @XmlElement
        List<WayPointData> route = new Vector<>();

        private EntityData(){}
        public EntityData(WorldState world, Entity original, boolean self){
            if(self) {
                charge = original.getCurrentBattery();
                load = original.getCurrentLoad();
                lastAction = new ActionData(original.getLastAction(), original.getLastActionResult());
                Facility fac = world.getFacilityByLocation(original.getLocation());
                if(fac != null) facility = fac.getName();
                Route route = original.getRoute();
                routeLength = route == null? 0: route.getWaypoints().size();
                ItemBox box = original.getInventory();
                box.getStoredTypes().forEach(item -> items.put(item.getName(), box.getItemCount(item)));
                if(route != null){
                    int i = 0;
                    for(Location loc: route.getWaypoints()){
                        this.route.add(new WayPointData(i++, loc.getLat(), loc.getLon()));
                    }
                }
            }
            else{
                team = world.getTeamForAgent(world.getAgentForEntity(original));
                role = original.getRole().getName();
            }
            name = world.getAgentForEntity(original);
            lat = original.getLocation().getLat();
            lon = original.getLocation().getLon();
        }
    }

    @XmlRootElement(name="n")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class WayPointData {
        @XmlAttribute(name="i")
        public int index;
        @XmlAttribute
        public double lat;
        @XmlAttribute
        public double lon;

        private WayPointData(){}
        public WayPointData(int index, double lat, double lon){
            this.index = index;
            this.lat = lat;
            this.lon = lon;
        }
    }

    @XmlRootElement(name="team")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class TeamData{
        @XmlAttribute
        public long money;
//        @XmlElement
//        public List<String> jobs;

        private TeamData(){}
        public TeamData(long money){
            this.money = money;
//            this.jobs = jobs;
        }
    }

    @XmlRootElement(name="action")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class ActionData{

        @XmlAttribute
        public String type;

        @XmlAttribute
        public String result;

        @XmlElement
        public List<String> params;

        private ActionData(){}
        public ActionData(Action lastAction, String lastActionResult) {
            type = lastAction.getType();
            result = lastActionResult;
            params = new Vector<>(lastAction.getParameters());
        }
    }

    @XmlRootElement(name="job")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class JobData{

        @XmlAttribute
        public String id;

        @XmlAttribute
        public String storage;

        @XmlAttribute
        public int end;

        @XmlElementWrapper(name="items")
        Map<String, Integer> requiredItems = new HashMap<>();

        private JobData(){}
        public JobData(Job job){
            id = job.getName();
            storage = job.getStorage().getName();
            end = job.getEndStep();
            job.getRequiredItems().entrySet().forEach(entry ->
                    requiredItems.put(entry.getKey().getName(), entry.getValue()));
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public abstract static class FacilityData{

        @XmlAttribute
        public String name;

        @XmlAttribute
        public double lat;

        @XmlAttribute
        public double lon;

        private FacilityData(){}
        public FacilityData(String name, double lat, double lon){
            this.name = name;
            this.lat = lat;
            this.lon = lon;
        }
    }

    @XmlRootElement(name="workshop")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class WorkshopData extends FacilityData{

        private WorkshopData(){}
        public WorkshopData(String name, double lat, double lon) {
            super(name, lat, lon);
        }
    }

    @XmlRootElement(name="charging")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class ChargingStationData extends FacilityData{

        @XmlAttribute
        public int rate;

        private ChargingStationData(){}
        public ChargingStationData(String name, double lat, double lon, int rate) {
            super(name, lat, lon);
            this.rate = rate;
        }
    }

    @XmlRootElement(name="dump")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class DumpData extends FacilityData{

        private DumpData(){}
        public DumpData(String name, double lat, double lon) {
            super(name, lat, lon);
        }
    }

    @XmlRootElement(name="shop")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class ShopData extends FacilityData{

        @XmlAttribute
        public int restock;

        @XmlElement(name="items")
        List<StockData> stocks = new Vector<>();

        private ShopData(){}
        public ShopData(Shop original) {
            super(original.getName(), original.getLocation().getLat(), original.getLocation().getLon());
            restock = original.getRestock();
            original.getOfferedItems().forEach(item ->
                    stocks.add(new StockData(item.getName(), original.getPrice(item), original.getItemCount(item))));
        }
    }

    @XmlRootElement(name="item")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class StockData{

        @XmlAttribute
        public String name;

        @XmlAttribute
        public int price;

        @XmlAttribute
        public int amount;

        private StockData(){}
        public StockData(String itemName, int price, int amount){
            name = itemName;
            this.price = price;
            this.amount = amount;
        }
    }

    @XmlRootElement(name="storage")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class StorageData extends FacilityData{

        @XmlAttribute
        public int totalCapacity;

        @XmlAttribute
        public int usedCapacity;

        @XmlElement
        List<StoredData> items = new Vector<>();

        private StorageData(){}
        public StorageData(Storage original, String team, WorldState world) {
            super(original.getName(), original.getLocation().getLat(), original.getLocation().getLon());
            totalCapacity = original.getCapacity();
            usedCapacity = totalCapacity - original.getFreeSpace();
            world.getItems().forEach(item -> {
                int stored = original.getStored(item, team);
                int delivered = original.getDelivered(item, team);
                if(stored != 0 || delivered != 0) items.add(new StoredData(item.getName(), stored, delivered));
            });
        }
    }

    @XmlRootElement(name="item")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class StoredData{

        @XmlAttribute
        public String name;

        @XmlAttribute
        public int stored;

        @XmlAttribute
        public int delivered;

        private StoredData(){}
        public StoredData(String item, int stored, int delivered){
            this.name = item;
            this.stored = stored;
            this.delivered = delivered;
        }
    }
}
