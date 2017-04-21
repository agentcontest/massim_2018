/**
 * 
 */
package massim.scenario.city;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;
import massim.protocol.scenario.city.util.LocationUtil;
import massim.util.Log;
import massim.util.RNG;
import massim.scenario.city.data.Location;
import massim.scenario.city.data.Route;
import massim.scenario.city.util.GraphHopperManager;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

/**
 * Holds the map and allows to access it.
 * @author fschlesinger
 */
public class CityMap implements Serializable {

	private double cellSize;
	private double minLat;
	private double maxLat;
	private double minLon;
	private double maxLon;
	private Location center;

	public CityMap(String mapName, double cellSize, double minLat, double maxLat, double minLon, double maxLon, Location center) {
		this.cellSize = cellSize;
		this.minLon = minLon;
		this.maxLon = maxLon;
		this.minLat = minLat;
		this.maxLat = maxLat;
		this.center = center;
		GraphHopperManager.init(mapName);
	}

	/**
	 * Calculates a new route between two locations.
	 * @param from the source location
	 * @param to the target location
	 * @param permissions the permissions to use
	 * @return a new route or null, if the permissions were incorrectly set or some other error occurred (e.g. no route exists).
	 */
    Route findRoute(Location from, Location to, Set<String> permissions){
		if(from == null || to == null) return null;
		if(!isInBounds(to)) return null; // target must be in bounds
		if (permissions.contains(GraphHopperManager.PERMISSION_AIR))
			return getNewAirRoute(from, to);
		if (permissions.contains(GraphHopperManager.PERMISSION_ROAD) && existsRoute(to, from))
			return getNewCarRoute(from, to);
		Log.log(Log.Level.ERROR, "Cannot find a route with those permissions");
		return null;
	}

    /**
     * Computes a new air route. GH not needed for this. Map bounds are not checked.
     * @param from source location
     * @param to target location
     * @return a new route or null if no such route exists
     */
	private Route getNewAirRoute(Location from, Location to){
		Route route = new Route();
		double fractions = getLength(from, to) / cellSize;
		Location loc = null;
		for (long i = 1; i <= fractions; i++) {
			loc = getIntermediateLoc(from, to, fractions, i);
			route.addPoint(loc);
		}
		if (!to.equals(loc)) { route.addPoint(to); }
		return route;
	}

    /**
     * Requests a (car) route from GH.
     * @param from starting location
     * @param to destination
     * @return the answer from GH
     */
	private GHResponse queryGH(Location from, Location to){
        GHRequest req = new GHRequest(from.getLat(), from.getLon(), to.getLat(), to.getLon())
                .setWeighting("shortest")
                .setVehicle("car");
        return GraphHopperManager.getHopper().route(req);
    }

    /**
     * Checks if a route exists without creating the actual route object.
     * @param from starting location
     * @param to destination
     * @return true if such a route exists
     */
    private boolean existsRoute(Location from, Location to) {
        GHResponse rsp = queryGH(from, to);
        rsp.getErrors().forEach(error -> System.out.println(error.getMessage()));
        return !rsp.hasErrors() && rsp.getBest().getPoints().size() > 0;
    }
	
	private Route getNewCarRoute(Location from, Location to){

        GHResponse rsp = queryGH(from, to);
		if(rsp.hasErrors()) return null;

		Route route = new Route();

		// points, distance in meters and time in millis of the full path
		PointList pointList = rsp.getBest().getPoints();
		Iterator<GHPoint3D> pIterator = pointList.iterator();
		if (!pIterator.hasNext()) return null;
		GHPoint prevPoint = pIterator.next();
		
		double remainder = 0;
		Location loc = null;
		while (pIterator.hasNext()){
			GHPoint nextPoint = pIterator.next();
			double length = getLength(prevPoint, nextPoint);
			if (length == 0){
				prevPoint = nextPoint;
				continue;
			}
			
			long i = 0;
			for (; i * cellSize + remainder < length ; i++) {
				loc = getIntermediateLoc(prevPoint, nextPoint, length, i * cellSize + remainder);
				if (!from.equals(loc)) {
					route.addPoint(loc);
				}
			}
			remainder = i * cellSize + remainder - length;
			prevPoint = nextPoint;
		}
		
		if (!to.equals(loc)) { route.addPoint(to); }

		return route;
	}
	
	
	private double getLength(Location loc1, Location loc2) {
        return LocationUtil.calculateRange(loc1.getLat(), loc1.getLon(), loc2.getLat(), loc2.getLon());
	}
	
	
	private Location getIntermediateLoc(Location loc1, Location loc2, double fractions, long i) {
		double lon = (loc2.getLon() - loc1.getLon())*i/fractions + loc1.getLon();
		double lat = (loc2.getLat() - loc1.getLat())*i/fractions + loc1.getLat();
		return new Location(lon,lat);
	}
	
	
	private double getLength(GHPoint loc1, GHPoint loc2) {
        return LocationUtil.calculateRange(loc1.getLat(), loc1.getLon(), loc2.getLat(), loc2.getLon());
	}
	
	private Location getIntermediateLoc(GHPoint loc1, GHPoint loc2, double length, double i) {
		double lon = (loc2.getLon() - loc1.getLon())*i/length + loc1.getLon();
		double lat = (loc2.getLat() - loc1.getLat())*i/length + loc1.getLat();
		return new Location(lon,lat);
	}

    /**
     * Gets a location on a road that is close(st) to the given location.
     * @param loc the location that should be snapped to a road.
     * @return a new location object fitting the description or null if there was no road found to snap to
     */
	private Location getNearestRoad(Location loc){
		QueryResult qr = GraphHopperManager.getHopper().getLocationIndex().findClosest(loc.getLat(), loc.getLon(),
				EdgeFilter.ALL_EDGES);
		try {
			GHPoint3D snap = qr.getSnappedPoint();
			return new Location(snap.getLon(), snap.getLat());
		} catch(IllegalStateException ignored){
            return null;
        }
	}

	/**
	 * Tries to find a random location on this map (reachable from the center).
	 * @param roads if not empty, tries to find a location snapped to these road types (e.g. "road")
	 * @param iterations if roads param is non-empty: maximum number of attempts to snap a random location to a road
	 * @return a random location on the map or the map's center, if no such location could be found
	 */
	public Location getRandomLocation(Set<String> roads, int iterations) {
		return getRandomLocationInBounds(roads, iterations, minLat, maxLat, minLon, maxLon);
	}

	/**
	 * Tries to find a random location on this map (reachable from the center) within some bounds.
     * <b>The bounds provided must be within map bounds.</b>
	 * @param roads if not empty, tries to find a location snapped to these road types (e.g. "road")
	 * @param iterations if roads param is non-empty: maximum number of attempts to snap a random location to a road
     * @param minLat the minimum latitude the area to search in
     * @param maxLat the maximum latitude of the area to search in
     * @param minLon the minimum longitude of the area to search in
     * @param maxLon the maximum longitude of the area to search in
     * @return a random location on the map or the map's center, if no such location could be found
	 */
	public Location getRandomLocationInBounds(Set<String> roads, int iterations,
                                              double minLat, double maxLat, double minLon, double maxLon) {
		Location loc;
		for (int i = 0; i < iterations; i++) {
			double latDiff = maxLat - minLat;
			double lonDiff = maxLon - minLon;
			double lat = minLat + RNG.nextDouble() * latDiff;
			double lon = minLon + RNG.nextDouble() * lonDiff;
			loc = getNearestRoad(new Location(lon, lat));
			if (isReachable(loc, roads)) return loc;
		}
		Log.log(Log.Level.ERROR, "Exceeded max tries to find a location.");
		return center;
	}

    /**
     * Checks if a location is reachable in this map. Needs to compute two routes for this.
     * @param loc the location to check
     * @param roads the roads that may be used
     * @return true if the location is reachable (i.e. it's not null, it's within map bounds,
	 * and the permissions contain "air" or a route to the center and back exists)
     */
	private boolean isReachable(Location loc, Set<String> roads) {
        if (loc == null || !isInBounds(loc)) return false;
        return roads.contains("air") || (existsRoute(loc, center) && existsRoute(center, loc));
    }

    /**
     * @param loc the location to check
     * @return true if the location is within map bounds
     */
    private boolean isInBounds(Location loc){
	    return loc.getLat() > minLat && loc.getLat() < maxLat && loc.getLon() > minLon && loc.getLon() < maxLon;
    }

	/**
	 * @return the "center" location that is used for reachability checking
	 */
	public Location getCenter(){
    	return center;
	}
}