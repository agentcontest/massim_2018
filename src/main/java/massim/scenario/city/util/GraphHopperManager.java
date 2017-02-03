package massim.scenario.city.util;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;

import java.io.File;

/**
 * Creates and holds the current GraphHopper instance.
 */
public class GraphHopperManager {

	public final static String PERMISSION_AIR = "air";
	public final static String PERMISSION_ROAD = "road";

	private static String mapName;
	private static GraphHopper hopper;

    /**
     * Creates a new GraphHopper for the given map name.
     * @param newMapName the name of the map to load
     */
	public static void init(String newMapName){
		
		if(newMapName.equals(mapName)) return;

		mapName = newMapName;
		hopper = new GraphHopper().forDesktop();
		hopper.setOSMFile("osm" + File.separator + mapName + ".osm.pbf");
		hopper.setCHEnabled(false); // CH does not work with shortest weighting (at the moment)
		
		// where to store GH files?
		hopper.setGraphHopperLocation("graphs" + File.separator + mapName);
		hopper.setEncodingManager(new EncodingManager("car"));

		// this may take a few minutes
		hopper.importOrLoad();
	}

    /**
     * @return the current GraphHopper instance
     */
	public static GraphHopper getHopper(){
		return hopper;
	}
}
