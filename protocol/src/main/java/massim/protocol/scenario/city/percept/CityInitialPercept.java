package massim.protocol.scenario.city.percept;

import massim.protocol.messagecontent.SimStart;
import massim.protocol.scenario.city.data.ItemData;
import massim.protocol.scenario.city.data.RoleData;
import massim.protocol.scenario.city.data.UpgradeData;
import massim.protocol.scenario.city.data.WellTypeData;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Vector;

/**
 * Represents a sim-start percept in the City scenario.
 * @author ta10
 */
@XmlRootElement(name="simulation")
@XmlAccessorType(XmlAccessType.NONE)
public class CityInitialPercept extends SimStart {

    @XmlAttribute private String name;
    @XmlAttribute(name="id") private String simId;
    @XmlAttribute private int steps;
    @XmlAttribute private String map;
    @XmlAttribute private long seedCapital;
    @XmlAttribute(name="team") private String teamName;
    @XmlAttribute private double minLat;
    @XmlAttribute private double maxLat;
    @XmlAttribute private double minLon;
    @XmlAttribute private double maxLon;
    @XmlAttribute private double centerLat;
    @XmlAttribute private double centerLon;
    @XmlAttribute private int proximity;
    @XmlAttribute private int cellSize;
    @XmlElement private RoleData role;

    @XmlElement(name="item") private List<ItemData> items;
    @XmlElement(name="upgrade") private List<UpgradeData> upgrades;
    @XmlElement(name="well") private List<WellTypeData> wellTypes;

    /**
     * for JAXB
     */
    private CityInitialPercept(){}

    /**
     * Constructor.
     * @param name name of the agent
     * @param simId id of the simulation
     * @param steps max number of steps
     * @param teamName name of the agent's team
     * @param mapName name of the current map
     * @param seedCapital money to start with
     * @param role role of the entity
     * @param items items available in the simulation
     * @param upgrades all available upgrades
     */
    public CityInitialPercept(String name, String simId, int steps, String teamName, String mapName, long seedCapital,
                              RoleData role, List<ItemData> items,
                              double minLat, double maxLat, double minLon, double maxLon,
                              double centerLat, double centerLon, int proximity, int cellSize,
                              List<WellTypeData> wellTypes, List<UpgradeData> upgrades) {
        this.name = name;
        this.simId = simId;
        this.steps = steps;
        this.teamName = teamName;
        this.map = mapName;
        this.seedCapital = seedCapital;
        this.role = role;
        this.items = items;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
        this.centerLat = centerLat;
        this.centerLon = centerLon;
        this.cellSize = cellSize;
        this.proximity = proximity;
        this.wellTypes = wellTypes;
        this.upgrades = upgrades;
    }

    public String getName(){
        return name;
    }

    public String getId(){
        return simId;
    }

    public int getSteps(){
        return steps;
    }

    public String getTeam(){
        return teamName;
    }

    public RoleData getRoleData(){
        return role;
    }

    public List<ItemData> getItemData(){
        return items == null? new Vector<>() : items;
    }

    public String getMapName(){
        return map;
    }

    public long getSeedCapital(){
        return seedCapital;
    }

    public double getMinLat(){return minLat;}

    public double getMaxLat(){return maxLat;}

    public double getMinLon(){return minLon;}

    public double getMaxLon(){return maxLon;}

    public double getCenterLat() {
        return centerLat;
    }

    public double getCenterLon() {
        return centerLon;
    }

    public int getProximity() {
        return proximity;
    }

    public double getCellSize() {
        return cellSize;
    }

    public List<WellTypeData> getWellTypes() {
        return wellTypes;
    }

    public List<UpgradeData> getUpgrades() {
        return upgrades;
    }
}