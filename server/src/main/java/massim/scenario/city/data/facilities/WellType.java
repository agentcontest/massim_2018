package massim.scenario.city.data.facilities;

/**
 * Describes the values a new well can have.
 */
public class WellType {

    private String name;
    private int initialIntegrity;
    private int maxIntegrity;
    private int cost;
    private int efficiency;

    public WellType(String name, int initialIntegrity, int maxIntegrity, int cost, int efficiency) {
        this.name = name;
        this.initialIntegrity = initialIntegrity;
        this.maxIntegrity = maxIntegrity;
        this.cost = cost;
        this.efficiency = efficiency;
    }

    public String getName() {
        return name;
    }

    public int getInitialIntegrity() {
        return initialIntegrity;
    }

    public int getMaxIntegrity() {
        return maxIntegrity;
    }

    public int getCost() {
        return cost;
    }

    public int getEfficiency() {
        return efficiency;
    }
}
