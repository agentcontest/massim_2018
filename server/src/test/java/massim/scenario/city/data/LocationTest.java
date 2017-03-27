package massim.scenario.city.data;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Testing Locations (for equality).
 */
public class LocationTest {

    /**
     * Checks whether location equality based on the proximity value works as intended.
     */
    @Test
    public void proximityWorks(){

        double lat = 10d;

        Location l1 = new Location(50.1111, lat);
        Location l2 = new Location(50.1112, lat);

        // check multiple proximity values

        Location.setProximity(3);
        assertTrue(l1.equals(l2));

        Location.setProximity(4);
        assertFalse(l1.equals(l2));
    }
}