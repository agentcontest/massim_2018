package massim.scenario.city.data;

import massim.util.Log;

import java.math.BigDecimal;

/**
 * Represents a map location in the city scenario.
 */
public class Location {

    private double lat;
    private double lon;
    private static int proximity;
    private static double divisor;

    public Location(double lon, double lat) {
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * @return the location's latitude
     */
    public double getLat() {
        return BigDecimal.valueOf(lat).setScale(proximity, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * @return the location's longitude
     */
    public double getLon() {
        return BigDecimal.valueOf(lon).setScale(proximity, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp = approximate(lat);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = approximate(lon);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj.getClass() != Location.class)
            return false;
        Location other = (Location) obj;
        return approximate(lat) == approximate(other.lat) && approximate(lon) == approximate(other.lon);
    }

    /**
     * Calculates a value of a coordinate to determine which coordinates are considered equal
     * @param coordinate the coordinate to approximate
     * @return the approximated value of the coordinate
     */
    private static long approximate(double coordinate){
        return Math.round(coordinate / divisor);
    }

    /**
     * Sets the global proximity value
     * @param newProximity the new proximity value
     */
    public static void setProximity(int newProximity){
        proximity = newProximity;
        divisor = 1d / Math.pow(10d, proximity);
    }

    /**
     * Tries to create a new Location object from the given (double) strings
     * @param latString latitude double
     * @param lonString longitude double
     * @return the location object or null if the strings are invalid
     */
    public static Location parse(String latString, String lonString) {
        try{
            double lat = Double.parseDouble(latString);
            double lon = Double.parseDouble(lonString);
            return new Location(lon, lat);
        } catch(NullPointerException | NumberFormatException e){
            Log.log(Log.Level.ERROR, "Invalid doubles: " + latString + " - " + lonString);
            return null;
        }
    }

    /**
     * Checks if two locations are "near" each other. Depends on {@link Location#proximity}.
     * @param other another location
     * @return whether both locations are considered equal/in range
     */
    public boolean inRange(Location other){
        if (other == null) return false;
        return this.equals(other);
    }
}
