package massim.scenario.city.util;

import massim.scenario.city.data.Item;
import massim.scenario.city.data.Job;
import massim.scenario.city.data.Location;
import massim.scenario.city.data.Tool;
import massim.scenario.city.data.facilities.Facility;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * Utility to generate random elements with.
 */
public class Generator {

    private Location mapCenter;

    public Generator(JSONObject randomConf){
        // TODO parse config
    }

    public List<Tool> generateTools(){
        //TODO
        return null;
    }

    public List<Item> generateItems(List<Tool> tools) {
        //TODO
        return dummyGenerateItems();
    }

    public List<Facility> generateFacilities() {
        //TODO
        return null;
    }

    /**
     * TODO: delete later
     */
    private List<Item> dummyGenerateItems() {
        List<Item> ret = new Vector<>();
        for (int i = 0; i < 20; i++){
            Item item = new Item("item"+i, 100, new HashSet<>());
            ret.add(item);
        }
        for (int i = 0; i < 10; i++){
            Item item = new Item("itemB" + i, 100, new HashSet<>());
            item.addRequirement(ret.get(2 * i), 1);
            item.addRequirement(ret.get(2 * i + 1), 1);
            ret.add(item);
        }
        return ret;
    }

    /**
     * @param stepNo the number of the current step
     */
    public Set<Job> generateJobs(int stepNo) {
        Set<Job> jobs = new HashSet<>();
        // TODO
        return jobs;
    }
}
