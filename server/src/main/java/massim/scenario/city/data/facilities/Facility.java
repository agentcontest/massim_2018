package massim.scenario.city.data.facilities;

import massim.scenario.city.data.Location;
import massim.util.NameComparator;

/**
 * Represents a generic facility in the city scenario.
 * @author ta10
 */
public abstract class Facility implements Comparable<Facility>{

    private String name;
    private Location location;

    public Facility(String name, Location location){
        this.name = name;
        this.location = location;
    }

    /**
     * @return the location object for this facility
     */
    public Location getLocation(){
        return location;
    }

    /**
     * @return the unique name of this facility
     */
    public String getName(){
        return name;
    }

    @Override
    public String toString(){
        return name + " loc(" + location.getLon() + "," + location.getLat() + ")";
    }

    @Override
    public int compareTo(Facility other){
        return new NameComparator().compare(name, other.name);
    }
}
