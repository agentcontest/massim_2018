package massim.scenario.city.util;

import massim.scenario.city.data.*;
import massim.scenario.city.data.facilities.*;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility to generate random elements with.
 */
public class Generator {

    public Generator(JSONObject randomConf){
        // TODO parse random parameters from config
    }

    /**
     * Generates exactly 5 tools. All are the same except for the name. This is probably something that should be changed.
     * @return 5 tools
     */
    public List<Tool> generateTools(List<Role> roles){
        //TODO
        List<Tool> tools = new Vector<>();
        for(int i = 0; i < 5; i++){
            tools.add(new Tool("Tool"+i, 100, roles.stream().map(Role::getName).collect(Collectors.toList()).toArray(new String[roles.size()])));
        }
        return tools;
    }

    public List<Item> generateItems(List<Tool> tools) {
        //TODO
        return dummyGenerateItems(tools);
    }

    public List<Facility> generateFacilities(List<Item> items, WorldState world) {
        //TODO implement for real
        //TODO ensure facilities have different locations
        List<Facility> result = new Vector<>();
        ChargingStation ch = new ChargingStation("Chargez", getRandomLocation(world), 100);
        result.add(ch);
        Dump dump = new Dump("Tronald's", getRandomLocation(world));
        result.add(dump);
        Storage storage = new Storage("Storage1", getRandomLocation(world), 10000,
                world.getTeams().stream().map(TeamState::getName).collect(Collectors.toSet()));
        result.add(storage);
        Workshop workshop = new Workshop("How to agent", getRandomLocation(world));
        result.add(workshop);
        Shop shop = new Shop("Shop", getRandomLocation(world), 1);
        shop.addItem(items.get(0), 3, 17171717);
        result.add(shop);
        return result;
    }

    /**
     * Tries to get a new random location with < 1000 attempts.
     * @param world the world to look for a location in
     * @return a new random location or the "center" of the map if no such location could be found in reasonable time
     */
    private Location getRandomLocation(WorldState world){
        return world.getMap().getRandomLocation(new HashSet<>(Collections.singletonList(GraphHopperManager.PERMISSION_ROAD)), 1000);
    }

    /**
     * TODO: delete later
     * @param tools this should be a list of exactly 5 tools which is mostly random anyway
     */
    private List<Item> dummyGenerateItems(List<Tool> tools) {
        List<Item> ret = new Vector<>();
        for (int i = 0; i < 20; i++){
            Item item = new Item("item"+i, 100, new HashSet<>());
            ret.add(item);
        }
        for (int i = 0; i < 10; i++){
            Item item = new Item("itemB" + i, 100, new HashSet<>());
            item.addRequirement(ret.get(2 * i), 1);
            item.addRequirement(ret.get(2 * i + 1), 1);
            item.addRequiredTool(tools.get(i/2));
            ret.add(item);
        }
        return ret;
    }

    /**
     * @param stepNo the number of the current step
     */
    public Set<Job> generateJobs(int stepNo, WorldState world) {
        Set<Job> jobs = new HashSet<>();
        // TODO maybe it's better not to create the same job each step
        jobs.add(new Job(1, Job.SOURCE_SYSTEM, world.getStorages().iterator().next(), stepNo + 1, stepNo + 10));
        return jobs;
    }
}
