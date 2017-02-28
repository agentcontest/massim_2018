package massim.scenario.city.util;

import massim.scenario.city.data.*;
import massim.scenario.city.data.facilities.*;
import massim.util.Log;
import massim.util.RNG;
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
        }else {

            //parse charging stations
            JSONObject chargingStations = facilities.optJSONObject("chargingStations");
            if (chargingStations == null) {
                Log.log(Log.Level.ERROR, "No charging stations in configuration.");
            } else {
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
     * Generates a number of tools dependent on config parameters
     * @return a list of tools
     */
    public List<Tool> generateTools(List<Role> roles){

        /*//TODO
        List<Tool> tools = new Vector<>();
        for(int i = 0; i < 5; i++){
            tools.add(new Tool("Tool"+i, 100, roles.stream().map(Role::getName).collect(Collectors.toList()).toArray(new String[roles.size()])));
        }
        //TODO add tools to their roles
        return tools;*/

        int toolAmount = RNG.nextInt((toolsMax-toolsMin)+1) + toolsMin;
        List<Tool> tools = new Vector<>();
        for(int i=0; i<toolAmount; i++){
            String name = "tool"+i;
            int volume = RNG.nextInt((maxVol-minVol)+1) + minVol;
            String role1;
            String role2;
            int randomRole = RNG.nextInt(roles.size());
            if(roles.get(randomRole).getMaxLoad()>volume) {
                role1=roles.get(randomRole).getName();
            }
            else{
                role1="Truck";
            }
            if(RNG.nextInt(100)<50){
                randomRole = RNG.nextInt(roles.size());
                if(roles.get(randomRole).getMaxLoad()>volume) {
                    role2=roles.get(randomRole).getName();
                    tools.add(new Tool(name, volume, role1, role2));
                    Log.log(Log.Level.NORMAL, "Configuring items tools: " + tools.get(i).getName() + ": volume=" + tools.get(i).getVolume() + " roles=" + tools.get(i).getRoles());
                    continue;
                }
            }
            tools.add(new Tool(name, volume, roles.get(randomRole).getName()));
            Log.log(Log.Level.NORMAL, "Configuring items tools: " + tools.get(i).getName() + ": volume=" + tools.get(i).getVolume() + " roles=" + tools.get(i).getRoles());
        }
        //TODO add tools to their roles
        return tools;
    }

    public List<Item> generateItems(List<Tool> tools) {
        int baseItemAmount = RNG.nextInt((baseItemsMax-baseItemsMin) + 1) + baseItemsMin;
        int resourcesAmount = RNG.nextInt((resourcesMax-resourcesMin) + 1) + resourcesMin;

        List<Item> items = new Vector<Item>();
        Vector<Item> baseItems = new Vector<Item>();
        Vector<Vector<Item>> itemGraph = new Vector<Vector<Item>>();
        List<Item> resources = new Vector<Item>();

        //generate base items
        for(int i=0; i<=baseItemAmount-1;i++){
            Item item = new Item("item"+i,RNG.nextInt((maxVol-minVol) + 1) + minVol, new HashSet<>());
            items.add(item);
            baseItems.add(item);
        }

        //generate resources
        for(int i=0; i<=resourcesAmount-1;i++){
            Item item = new Item("item"+(baseItemAmount+i),RNG.nextInt((maxVol-minVol) + 1) + minVol, new HashSet<>());
            items.add(item);
            resources.add(item);
            baseItems.add(item);
        }
        itemGraph.add(baseItems);

        //generate assembled items
        int graphDepth = RNG.nextInt((graphDepthMax-graphDepthMin) + 1) + graphDepthMin;
        int levelAmount = baseItemAmount; //only base items without resources otherwise graph gets to big!
        int counter = baseItemAmount + resourcesAmount;

        for(int i=1; i<=graphDepth; i++){
            levelAmount = levelAmount - (RNG.nextInt((levelDecreaseMax-levelDecreaseMin) + 1) + levelDecreaseMin);
            Vector<Item> levelItems = new Vector<Item>();
            for(int j=1; j<=levelAmount;j++){

                //generate required items
                Map<Item, Integer> requiredItems = new HashMap<>();
                int requiredAmount = RNG.nextInt((maxReq-minReq) + 1) + minReq;
                //add item from one level beneath, if level beneath is level 0, take a resource
                Vector<Item> tmpItems;
                if (i - 1 == 0) {
                    tmpItems = new Vector<Item>(resources);
                } else {
                    tmpItems = new Vector<Item>(itemGraph.get(i - 1));
                }
                RNG.shuffle(tmpItems);
                requiredItems.put(tmpItems.get(0),RNG.nextInt((reqAmountMax-reqAmountMin) + 1) + reqAmountMin);
                requiredAmount = requiredAmount - 1;
                //get list of possible levels and possible items
                Vector<Vector<Item>> possibleLevels = new Vector<Vector<Item>>();
                for (int k = 0; k < i; k++) {
                    possibleLevels.add(itemGraph.get(k));
                }
                ArrayList<Item> possibleItems = new ArrayList<Item>();
                for (Vector<Item> level : possibleLevels) {
                    for (Item possibleItem : level) {
                        possibleItems.add(possibleItem);
                    }
                }
                possibleItems.remove(tmpItems.get(0)); //remove the item that was already added in the first step
                RNG.shuffle(possibleItems);
                //add amount of required items
                for (int l = 0; l < requiredAmount; l++) {
                    requiredItems.put(possibleItems.get(l), RNG.nextInt((reqAmountMax-reqAmountMin) + 1) + reqAmountMin);
                }

                //generate required tools
                Vector<Tool> requiredTools = new Vector<Tool>();
                if(RNG.nextDouble()<toolProbability){
                    RNG.shuffle(tools);
                    requiredTools.add(tools.get(0));
                    if(RNG.nextDouble()<toolProbability){
                        requiredTools.add(tools.get(1));
                    }
                }

                //TODO compute volume of assembled item
                int volume = 0;


                //generate assembled item
                Item item = new Item("item"+counter, volume, new HashSet<>());
                for(Item reqItem: requiredItems.keySet()){
                    item.addRequirement(reqItem, requiredItems.get(reqItem));
                }
                for(Tool reqTool: requiredTools){
                    item.addRequiredTool(reqTool);
                }
                items.add(item);
                levelItems.add(item);
                counter++;
            }
            itemGraph.add(levelItems);
        }

        int counter2=0;
        for(Vector<Item> itemList: itemGraph){
            Log.log(Log.Level.NORMAL, "Configuring items: item graph level " + counter2);
            for(Item item: itemList){
                Vector<String> reqItems = new Vector<String>();
                for(Item reqItem: item.getRequiredItems().keySet()){
                    reqItems.add(reqItem.getName());
                    //System.out.println(reqItem.getName() + ": " + item.getRequiredItems().get(reqItem));
                }
                Vector<String> reqTools = new Vector<String>();
                for(Tool reqTool: item.getRequiredTools()){
                    reqTools.add(reqTool.getName());
                }
                Log.log(Log.Level.NORMAL, "Configuring items: " + item.getName() + " volume=" + item.getVolume() + " items=" + String.join(",", reqItems) + " tools=" + String.join(",", reqTools));
            }
            counter2++;
        }
        return items;

        //return dummyGenerateItems(tools);
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
