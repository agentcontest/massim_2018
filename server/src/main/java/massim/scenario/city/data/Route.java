package massim.scenario.city.data;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a route in the City scenario (from one {@link Location} to another).
 */
public class Route {

    private LinkedList<Location> route;

    /**
     * Creates an empty route.
     */
    public Route() {
        route = new LinkedList<>();
    }

    /**
     * Adds the given waypoint to the route.
     * @param loc a location to add as next waypoint
     */
    public void addPoint(Location loc){
        route.add(loc);
    }

    /**
     * @return true if the route has no points/locations left
     */
    public boolean isCompleted(){
        return route.isEmpty();
    }

    /**
     * Moves along the route.
     * @param speed the speed to move at.
     * @return the new location on this route.
     */
    public Location advance(int speed){
        Location loc = null;
        int steps = speed;
        while (steps-- > 0 && !route.isEmpty()){
            loc = route.poll();
        }
        return loc;
    }

    /**
     * @param speed the hypothetical speed to move along this route
     * @return the number of steps it takes to complete the route at the given speed
     */
    public int getRouteDuration(int speed){
        int length = getRouteLength();
        return (length / speed) + (length % speed > 0 ? 1:0); // count possible remainder as additional step
    }

    /**
     * @return a (new) list containing the waypoints of this route
     */
    public List<Location> getWaypoints(){
        return new LinkedList<>(route);
    }

    /**
     * @return the number of waypoints in this route
     */
    public int getRouteLength(){
        return route == null? 0: route.size();
    }
}
