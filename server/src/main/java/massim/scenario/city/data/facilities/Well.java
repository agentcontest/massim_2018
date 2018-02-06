package massim.scenario.city.data.facilities;

import massim.scenario.city.data.Location;

/**
 * A well in the City scenario. Generates points in each step.
 */
public class Well extends Facility {

    private int integrity;
    private int maxIntegrity;
    private int efficiency;
    private boolean completed = false;
    private int cost;
    private String team;

    public Well(String name, Location location, WellType type) {
        super(name, location);
        this.efficiency = type.getEfficiency();
        this.integrity = type.getInitialIntegrity();
        this.maxIntegrity = type.getMaxIntegrity();
        this.cost = type.getCost();
    }

    public void build(int skill) {
        integrity = Math.min(integrity + skill, maxIntegrity);
        if(!completed && integrity == maxIntegrity) completed = true;
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
        return efficiency;
    }

    public String getTeam() {
        return team;
    }

    public int getCost() {
        return cost;
    }

}
