package massim.scenario.city.data.facilities;

import massim.scenario.city.data.Location;

/**
 * Created in 2017.
 */
public class ChargingStation extends Facility {

    private int rate;

    public ChargingStation(String name, Location location, int rate) {
        super(name, location);
        this.rate = rate;
    }

    public int getRate(){
        return rate;
    }

    @Override
    public String toString(){
        return super.toString() + " rate(" + rate + ")";
    }
}
