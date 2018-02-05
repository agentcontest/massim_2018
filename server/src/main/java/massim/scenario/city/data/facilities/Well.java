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

    public Well(String name, Location location, WellType type) {
        super(name, location);
        this.efficiency = type.efficiency;
        this.integrity = type.initialIntegrity;
        this.maxIntegrity = type.maxIntegrity;
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

    public void setIntegrity(int integrity) {
        this.integrity = integrity;
    }

    public int getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(int efficiency) {
        this.efficiency = efficiency;
    }

    public class WellType {
        public String name;
        public int initialIntegrity;
        public int maxIntegrity;
        public int cost;
        public int efficiency;
    }
}
