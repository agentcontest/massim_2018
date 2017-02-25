package massim.scenario.city.util;

import massim.scenario.city.data.*;
import massim.scenario.city.data.facilities.*;
import massim.util.Log;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility to generate random elements with.
 */
public class Generator {

    private int baseItemsMin;
    private int baseItemsMax;
    private int levelDecreaseMin;
    private int levelDecreaseMax;
    private int graphDepthMin;
    private int graphDepthMax;
    private int resourcesMin;
    private int resourcesMax;

    private int minVol;
    private int maxVol;
    private int valueMin;
    private int valueMax;
    private int minReq;
    private int maxReq ;
    private int reqAmountMin;
    private int reqAmountMax;

    private int toolsMin;
    private int toolsMax;
    private double toolProbability;


    public Generator(JSONObject randomConf){
        // TODO parse random parameters from config

        //parse facilities
        JSONObject facilities = randomConf.optJSONObject("facilities");
        if(facilities == null){
            Log.log(Log.Level.ERROR, "No facilities in configuration.");
        }else{

            //parse charging stations
            JSONObject chargingStations = facilities.optJSONObject("chargingStations");
            if(chargingStations == null){
                Log.log(Log.Level.ERROR, "No charging stations in configuration.");
            }else{
                String density = chargingStations.optString("density", "0.8");
                Log.log(Log.Level.NORMAL, "Configuring facilities charging station density: " + density);
            }
            //parse shops

            //parse dumps

            //parse workshops

            //parse storage
        }


        //parse items
        JSONObject items = randomConf.optJSONObject("items");
        if(items == null){
            Log.log(Log.Level.ERROR, "No items in configuration.");
        }else{
            baseItemsMin = items.optInt("baseItemsMin",5);
            Log.log(Log.Level.NORMAL, "Configuring items baseItemsMin: " + baseItemsMin);
            baseItemsMax = items.optInt("baseItemsMax",7);
            Log.log(Log.Level.NORMAL, "Configuring items baseItemsMax: " + baseItemsMax);
            levelDecreaseMin = items.optInt("levelDecreaseMin",1);
            Log.log(Log.Level.NORMAL, "Configuring items levelDecreaseMin: " + levelDecreaseMin);
            levelDecreaseMax = items.optInt("levelDecreaseMax",2);
            Log.log(Log.Level.NORMAL, "Configuring items levelDecreaseMax: " + levelDecreaseMax);
            graphDepthMin = items.optInt("graphDepthMin",3);
            Log.log(Log.Level.NORMAL, "Configuring items graphDepthMin: " + graphDepthMin);
            graphDepthMax = items.optInt("graphDepthMax",4);
            Log.log(Log.Level.NORMAL, "Configuring items graphDepthMax: " + graphDepthMax);
            resourcesMin = items.optInt("resourcesMin",1);
            Log.log(Log.Level.NORMAL, "Configuring items resourcesMin: " + resourcesMin);
            resourcesMax = items.optInt("resourcesMax",1);
            Log.log(Log.Level.NORMAL, "Configuring items resourcesMax: " + resourcesMax);

            minVol = items.optInt("minVol",10);
            Log.log(Log.Level.NORMAL, "Configuring items minVol: " + minVol);
            maxVol = items.optInt("maxVol",100);
            Log.log(Log.Level.NORMAL, "Configuring items maxVol: " + maxVol);
            valueMin = items.optInt("valueMin",10);
            Log.log(Log.Level.NORMAL, "Configuring items valueMin: " + valueMin);
            valueMax = items.optInt("valueMax",100);
            Log.log(Log.Level.NORMAL, "Configuring items valueMax: " + valueMax);
            minReq = items.optInt("minReq",1);
            Log.log(Log.Level.NORMAL, "Configuring items minReq: " + minReq);
            maxReq = items.optInt("maxReq",3);
            Log.log(Log.Level.NORMAL, "Configuring items maxReq: " + maxReq);
            reqAmountMin = items.optInt("reqAmountMin",1);
            Log.log(Log.Level.NORMAL, "Configuring items reqAmountMin: " + reqAmountMin);
            reqAmountMax = items.optInt("reqAmountMax",3);
            Log.log(Log.Level.NORMAL, "Configuring items reqAmountMax: " + reqAmountMax);

            toolsMin = items.optInt("toolsMin",1);
            Log.log(Log.Level.NORMAL, "Configuring items toolsMin: " + toolsMin);
            toolsMax = items.optInt("toolsMax",1);
            Log.log(Log.Level.NORMAL, "Configuring items toolsMax: " + toolsMax);
            toolProbability = items.optDouble("toolProbability",0.5);
            Log.log(Log.Level.NORMAL, "Configuring items toolProbability: " + toolProbability);
        }

        //parse jobs
        JSONObject jobs = randomConf.optJSONObject("jobs");
        if(jobs == null){
            Log.log(Log.Level.ERROR, "No jobs in configuration.");
        }else{

        }
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
        //TODO add tools to their roles
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
        jobs.add(new Job(1, world.getStorages().iterator().next(), stepNo + 1, stepNo + 10));
        return jobs;
    }
}
