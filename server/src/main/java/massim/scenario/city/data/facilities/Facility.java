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
    private int blackoutCounter;

    public Facility(String name, Location location){
        this.name = name;
        this.location = location;
        this.blackoutCounter = 0;
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

    /**
     * causes a blackout for this facility
     * @param duration the duration of the blackout
     */
    public void initiateBlackout(int duration) { blackoutCounter = duration; }

    /**
     * Decrements blackout counter if possible.
     * @return the current value of the blackout counter after the decrement
     */
    public int stepBlackoutCounter(){
        if(blackoutCounter > 0) blackoutCounter--;
        return blackoutCounter;
    }

    /**
     * @return the value of the blackoutCounter of this facility
     */
    public int getBlackoutCounter(){ return blackoutCounter; }

    @Override
    public String toString(){
        return name + " loc(" + location.getLon() + "," + location.getLat() + ")";
    }

    @Override
    public int compareTo(Facility other){
        return new NameComparator().compare(name, other.name);
    }
}
