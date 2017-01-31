package massim.scenario.city.util;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;

import java.io.File;

/**
 * Creates and holds the current GraphHopper instance.
 */
public class GraphHopperManager {

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
