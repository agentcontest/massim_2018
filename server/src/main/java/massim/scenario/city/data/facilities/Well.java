package massim.scenario.city.data.facilities;

import massim.protocol.scenario.city.data.WellData;
import massim.scenario.city.data.Location;

/**
 * A well in the City scenario. Generates points in each step.
 */
public class Well extends Facility {

    private int integrity;
    private boolean completed = false;
    private String team;
    private WellType type;

    public Well(String name, String team, Location location, WellType type) {
        super(name, location);
        this.team = team;
        this.type = type;
        this.integrity = type.getInitialIntegrity();
    }

    public void build(int skill) {
        integrity = Math.min(integrity + skill, type.getMaxIntegrity());
        if(!completed && integrity == type.getMaxIntegrity()) completed = true;
    }

    public boolean dismantle(int skill) {
        integrity = Math.max(integrity - skill, 0);
        return integrity == 0;
    }

    public boolean generatesPoints() {
        return completed && integrity > 0;
    }

    public int getIntegrity() {
        return integrity;
    }

    public int getEfficiency() {
        return type.getEfficiency();
    }

    public String getTeam() {
        return team;
    }

    public int getCost() {
        return type.getCost();
    }

    public int getMaxIntegrity() {
        return type.getMaxIntegrity();
    }

    public String getTypeName() {
        return type.getName();
    }

    public WellData toWellData() {
        return new WellData(getName(), getLocation().getLat(), getLocation().getLon(), team, type.getName(), integrity);
    }
}
