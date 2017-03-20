package massim.protocol.scenario.city.util;

/**
 * Provides location-related functionality.
 */
public abstract class LocationUtil {

    /**
     * Calculates the actual distance between two locations.
     * @param lat1 latitude of location 1
     * @param lon1 longitude of location 1
     * @param lat2 latitude of location 2
     * @param lon2 longitude of location 2
     * @return the distance between the two locations in meters
     */
    public static double calculateRange(double lat1, double lon1, double lat2, double lon2){
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);

        double a = Math.pow(Math.sin(deltaPhi / 2), 2);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.pow(Math.sin(deltaLambda / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371e3 * c;
    }
}
