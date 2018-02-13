package massim.scenario.city.data;

import massim.protocol.scenario.city.data.UpgradeData;

/**
 * An upgrade.
 */
public class Upgrade {

    private String name;
    private int cost;
    private int step;

    public Upgrade(String name, int cost, int step) {
        this.name = name;
        this.cost = cost;
        this.step = step;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public int getStep() {
        return step;
    }

    public UpgradeData toUpgradeData() {
        return new UpgradeData(name, cost, step);
    }
}
