package massim.scenario.city.util;

import massim.protocol.scenario.city.data.JobData;
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

    private double quadSize;

    private double chargingDensity;
    private int rateMin;
    private int rateMax;

    // shop parameters
    private double shopDensity;
    private int tradeModMin;
    private int tradeModMax;

    private double dumpDensity;

    private double workshopDensity;

    private double storageDensity;
    private int capacityMin;
    private int capacityMax;

    private double resourceDensity;
    private int thresholdMin;
    private int thresholdMax;

    // well type parameters
    private int wellTypesMin;
    private int wellTypesMax;
    private int baseEfficiencyMin;
    private int baseEfficiencyMax;
    private int efficiencyIncreaseMin;
    private int efficiencyIncreaseMax;
    private int baseIntegrityMin;
    private int baseIntegrityMax;
    private int costFactor;

    // item parameters
    private int levelDecreaseMin;
    private int levelDecreaseMax;
    private int graphDepthMin;
    private int graphDepthMax;
    private int resourcesMin;
    private int resourcesMax;
    private int volMin;
    private int volMax;
    private int partsMin;
    private int partsMax;

    // job parameters
    private double jobProbability;
    private double auctionProbability;
    private double missionProbability;
    private int jobDurationMin;
    private int jobDurationMax;
    private int rewardModMin;
    private int rewardModMax;
    private int rewardScale;
    private int itemCountMin;
    private int itemCountMax;

    private int auctionTime;

    private int missionID = 0;

    public Generator(JSONObject randomConf){
        //parse facilities
        JSONObject facilities = randomConf.optJSONObject("facilities");
        if(facilities == null) {
            Log.log(Log.Level.ERROR, "No facilities in configuration.");
        } else {
            quadSize = facilities.optDouble("quadSize", 0.4);
            Log.log(Log.Level.NORMAL, "Configuring facilities quadSize: " + quadSize);

            //parse charging stations
            JSONObject chargingStations = facilities.optJSONObject("chargingStations");
            if (chargingStations == null) {
                Log.log(Log.Level.ERROR, "No charging stations in configuration.");
            } else {
                chargingDensity = chargingStations.optDouble("density", 0.9);
                Log.log(Log.Level.NORMAL, "Configuring facilities charging station density: " + chargingDensity);
                rateMin = chargingStations.optInt("rateMin", 50);
                Log.log(Log.Level.NORMAL, "Configuring facilities charging station rateMin: " + rateMin);
                rateMax = chargingStations.optInt("rateMax", 150);
                Log.log(Log.Level.NORMAL, "Configuring facilities charging station rateMax: " + rateMax);
            }

            //parse shops
            JSONObject shops = facilities.optJSONObject("shops");
            if (shops == null) {
                Log.log(Log.Level.ERROR, "No shops in configuration.");
            } else {
                shopDensity = shops.optDouble("density", 0.8);
                Log.log(Log.Level.NORMAL, "Configuring facilities shop density: " + shopDensity);
                tradeModMin = optInt(shops, "tradeModMin", 1);
                tradeModMax = optInt(shops, "tradeModMax", 2);
            }

            //parse dumps
            JSONObject dumps = facilities.optJSONObject("dumps");
            if (dumps == null) {
                Log.log(Log.Level.ERROR, "No dumps in configuration.");
            } else {
                dumpDensity = dumps.optDouble("density", 0.6);
                Log.log(Log.Level.NORMAL, "Configuring facilities dump density: " + dumpDensity);
            }

            //parse workshops
            JSONObject workshops = facilities.optJSONObject("workshops");
            if (workshops == null) {
                Log.log(Log.Level.ERROR, "No workshops in configuration.");
            } else {
                workshopDensity = workshops.optDouble("density", 0.6);
                Log.log(Log.Level.NORMAL, "Configuring facilities workshops density: " + workshopDensity);
            }

            //parse storage
            JSONObject storage = facilities.optJSONObject("storage");
            if (storage == null) {
                Log.log(Log.Level.ERROR, "No storage in configuration.");
            } else {
                storageDensity = storage.optDouble("density", 0.8);
                Log.log(Log.Level.NORMAL, "Configuring facilities storage density: " + storageDensity);
                capacityMin = storage.optInt("capacityMin", 7000);
                Log.log(Log.Level.NORMAL, "Configuring facilities storage capacityMin: " + capacityMin);
                capacityMax = storage.optInt("capacityMax", 10000);
                Log.log(Log.Level.NORMAL, "Configuring facilities storage capacityMax: " + capacityMax);
            }

            //parse resourceNodes
            JSONObject resourceNodes = facilities.optJSONObject("resourceNodes");
            if (resourceNodes == null) {
                Log.log(Log.Level.ERROR, "No resource nodes in configuration.");
            } else {
                resourceDensity = resourceNodes.optDouble("density", 0.7);
                Log.log(Log.Level.NORMAL, "Configuring facilities resource node density: " + resourceDensity);
                thresholdMin = optInt(resourceNodes, "thresholdMin", 10);
                thresholdMax = optInt(resourceNodes, "thresholdMax", 30);
            }

            //parse well type info
            JSONObject welltypes = facilities.optJSONObject("wells");
            if (resourceNodes == null) {
                Log.log(Log.Level.ERROR, "No well types configured.");
            } else {
                wellTypesMin = optInt(welltypes, "wellTypesMin", 2);
                wellTypesMax = optInt(welltypes, "wellTypesMax", 5);
                baseEfficiencyMin = optInt(welltypes, "baseEfficiencyMin", 1);
                baseEfficiencyMax = optInt(welltypes, "baseEfficiencyMax", 5);
                efficiencyIncreaseMin = optInt(welltypes, "efficiencyIncreaseMin", 1);
                efficiencyIncreaseMax = optInt(welltypes, "efficiencyIncreaseMax", 5);
                baseIntegrityMin = optInt(welltypes, "baseIntegrityMin", 10);
                baseIntegrityMax = optInt(welltypes, "baseIntegrityMax", 20);
                costFactor = optInt(welltypes, "costFactor", 100);
            }
        }

        //parse items
        JSONObject items = randomConf.optJSONObject("items");
        if(items == null) {
            Log.log(Log.Level.ERROR, "No items in configuration.");
        } else {
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

            volMin = optInt(items, "volMin", 10);
            volMax = optInt(items, "volMax", 100);
            partsMin = optInt(items, "partsMin", 2);
            partsMax = optInt(items, "partsMax", 8);
        }

        //parse jobs
        JSONObject jobs = randomConf.optJSONObject("jobs");
        if(jobs == null) {
            Log.log(Log.Level.ERROR, "No jobs in configuration.");
        } else {
            jobProbability = optDouble(jobs, "jobProbability", 0.06);
            auctionProbability = optDouble(jobs, "auctionProbability", 0.02);
            missionProbability = optDouble(jobs, "missionProbability", 0.01);
            jobDurationMin = optInt(jobs, "jobDurationMin", 50);
            jobDurationMax = optInt(jobs, "jobDurationMax", 100);
            rewardModMin = optInt(jobs, "rewardModMin", 10);
            rewardModMax = optInt(jobs, "rewardModMax", 20);
            rewardScale = optInt(jobs, "rewardScale", 10);
            itemCountMin = optInt(jobs, "itemCountMin", 2);
            itemCountMax = optInt(jobs, "itemCountMax", 10);

            //parse auctions
            JSONObject auctions = jobs.optJSONObject("auctions");
            if (auctions == null) {
                Log.log(Log.Level.ERROR, "No auctions in configuration.");
            } else {
                auctionTime = optInt(auctions, "auctionTime", 5);
            }
        }
    }

    /**
     * Reads and logs an int value from a JSON object.
     * @param src the object to read
     * @param key the key to use
     * @param standard the default value
     * @return the read integer or the default value
     */
    private int optInt(JSONObject src, String key, int standard) {
        int k = src.optInt(key, standard);
        Log.log(Log.Level.NORMAL, "Config: " + key + " set to " + k);
        return k;
    }

    private double optDouble(JSONObject src, String key, double standard) {
        double k = src.optDouble(key, standard);
        Log.log(Log.Level.NORMAL, "Config: " + key + " set to " + k);
        return k;
    }

    /**
     * Generates a number of items dependent on config parameters
     * @return a list of items
     */
    public List<Item> generateItems(List<Role> r) {

        List<Item> items = new ArrayList<>();
        List<Role> roles = new ArrayList<>(r);

        // generate base items/resources
        for(int i = 0; i < between(resourcesMin, resourcesMax); i++){
            Item item = new Item("item" + i, between(volMin, volMax),
                        new HashSet<>(), new HashSet<>());
            items.add(item);
        }

        // generate assembled items
        int layers = between(graphDepthMin, graphDepthMax);
        int levelAmount = items.size();
        int itemCount = items.size();
        for(int i = 1; i <= layers; i++){
            levelAmount = Math.max(1, levelAmount - between(levelDecreaseMin, levelDecreaseMax));
            List<Item> layerItems = new ArrayList<>();
            for(int j = 1; j <= levelAmount; j++){
                // draw random parts from all items on previous layers
                int numberOfParts = between(partsMin, partsMax);
                List<Item> possibleParts = new ArrayList<>(items);
                RNG.shuffle(possibleParts);
                Set<Item> parts = new HashSet<>(possibleParts.subList(0, Math.min(numberOfParts, possibleParts.size())));
                // determine required roles
                RNG.shuffle(roles);
                Set<Role> requiredRoles = new HashSet<>(roles.subList(0, Math.min(2, roles.size())));

                Item item = new Item("item" + itemCount++, between(volMin, volMax), parts, requiredRoles);
                layerItems.add(item);
            }
            items.addAll(layerItems);
        }
        items.forEach(item -> Log.log(Log.Level.NORMAL, String.format("%s: vol(%d), val(%d)",
                item.getName(), item.getVolume(), item.getValue())));
        return items;
    }

    /**
     * Generates a number of facilities dependent on config parameters
     * @return a list of facilities
     */
    public List<Facility> generateFacilities(WorldState world) {
        double minLat = world.getMinLat();
        double maxLat = world.getMaxLat();
        double minLon = world.getMinLon();
        double maxLon = world.getMaxLon();

        List<Facility> facilities = new ArrayList<>();
        List<Shop> shops = new ArrayList<>();
        Set<Location> locations = new HashSet<>();

        // generate charging stations
        int chargingCounter = 0;
        for (double a = minLat; a < maxLat; a += quadSize) {
            for (double b = minLon; b < maxLon; b += quadSize) { // (a,b) = corner of the current quadrant

                int numberOfFacilities = 0;
                if(chargingDensity < 1){
                    if(RNG.nextDouble() < shopDensity) numberOfFacilities = 1;
                }
                else numberOfFacilities = new Float(chargingDensity).intValue();

                for(int i = 0; i < numberOfFacilities; i++){
                    ChargingStation charging = new ChargingStation("chargingStation" + chargingCounter,
                            getUniqueLocationInBounds(locations, world, a, a + quadSize, b, b + quadSize),
                            RNG.nextInt(rateMax - rateMin + 1) + rateMin);
                    facilities.add(charging);
                    locations.add(charging.getLocation());
                    chargingCounter++;
                }
            }
        }

        if(chargingCounter == 0){ // create at least 1 charging station
            ChargingStation charging = new ChargingStation("chargingStation" + chargingCounter,
                    getUniqueLocation(locations, world),
                    RNG.nextInt((rateMax-rateMin) + 1) + rateMin);
            facilities.add(charging);
            locations.add(charging.getLocation());
        }

        // generate shops
        for(double a = minLat; a < maxLat; a += quadSize) {
            for (double b = minLon; b < maxLon; b += quadSize) { // (a,b) = corner of the current quadrant
                int numberOfFacilities = 0;
                if(shopDensity < 1){
                    if(RNG.nextDouble() < shopDensity) numberOfFacilities = 1;
                } else {
                    numberOfFacilities = new Float(shopDensity).intValue();
                }
                for(int i = 0; i < numberOfFacilities; i++) {
                    Location loc = getUniqueLocationInBounds(locations, world, a, a + quadSize, b, b + quadSize);
                    Shop shop = new Shop("shop" + shops.size(), loc, between(tradeModMin, tradeModMax));
                    facilities.add(shop);
                    locations.add(shop.getLocation());
                    shops.add(shop);
                }
            }
        }
        if(shops.size() == 0){
            Shop shop = new Shop("shop" + shops.size(), getUniqueLocation(locations, world), between(tradeModMin, tradeModMax));
            facilities.add(shop);
            locations.add(shop.getLocation());
            shops.add(shop);
        }

        // generate dumps
        int dumpCounter = 0;
        for (double a = minLat; a < maxLat; a += quadSize) {
            for (double b = minLon; b < maxLon; b += quadSize) { // (a,b) = corner of the current quadrant
                int numberOfFacilities = 0;
                if(dumpDensity < 1){
                    if(RNG.nextDouble() < dumpDensity) numberOfFacilities = 1;
                }
                else numberOfFacilities = new Float(dumpDensity).intValue();
                for(int i = 0; i < numberOfFacilities; i++){
                    Location loc = getUniqueLocationInBounds(locations, world, a, a + quadSize, b, b + quadSize);
                    Dump dump1 = new Dump("dump" + dumpCounter, loc);
                    facilities.add(dump1);
                    locations.add(dump1.getLocation());
                    dumpCounter++;
                }
            }
        }
        if(dumpCounter == 0){
            Dump dump = new Dump("dump" + dumpCounter, getUniqueLocation(locations, world));
            facilities.add(dump);
            locations.add(dump.getLocation());
        }

        //generate workshops
        int workshopCounter = 0;
        for (double a = minLat; a < maxLat; a += quadSize) {
            for (double b = minLon; b < maxLon; b += quadSize) { // (a,b) = corner of the current quadrant
                int numberOfFacilities = 0;
                if(workshopDensity < 1){
                    if(RNG.nextDouble() < workshopDensity) numberOfFacilities = 1;
                }
                else numberOfFacilities = new Float(workshopDensity).intValue();
                for(int i = 0; i < numberOfFacilities; i++){
                    Location loc = getUniqueLocationInBounds(locations, world, a, a + quadSize, b, b + quadSize);
                    Workshop workshop = new Workshop("workshop" + workshopCounter, loc);
                    facilities.add(workshop);
                    locations.add(workshop.getLocation());
                    workshopCounter++;
                }
            }
        }
        if(workshopCounter == 0){
            Workshop workshop = new Workshop("workshop" + workshopCounter, getUniqueLocation(locations, world));
            facilities.add(workshop);
            locations.add(workshop.getLocation());
        }

        // generate storage
        int storageCounter = 0;
        for (double a = minLat; a < maxLat; a += quadSize) {
            for (double b = minLon; b < maxLon; b += quadSize) { // (a,b) = corner of the current quadrant
                int numberOfFacilities = 0;
                if(storageDensity < 1){
                    if(RNG.nextDouble() < storageDensity) numberOfFacilities = 1;
                }
                else numberOfFacilities = new Float(storageDensity).intValue();
                for(int i = 0; i < numberOfFacilities; i++){
                    Location loc = getUniqueLocationInBounds(locations, world, a, a + quadSize, b, b + quadSize);
                    Storage storage = new Storage("storage" + storageCounter, loc,
                            (RNG.nextInt(capacityMax - capacityMin + 1) + capacityMin),
                            world.getTeams().stream().map(TeamState::getName).collect(Collectors.toSet()));
                    facilities.add(storage);
                    locations.add(storage.getLocation());
                    storageCounter++;
                }
            }
        }
        if(storageCounter == 0){
            Storage storage = new Storage("storage" + storageCounter, getUniqueLocation(locations, world),
                    (RNG.nextInt(capacityMax - capacityMin + 1) + capacityMin),
                    world.getTeams().stream().map(TeamState::getName).collect(Collectors.toSet()));
            facilities.add(storage);
            locations.add(storage.getLocation());
        }

        // generate resource nodes
        int rnCounter = 0;
        // generate at least 1 resource node for each resource
        for (Item item : world.getResources()) {
            Location loc = getUniqueLocation(locations, world);
            ResourceNode node = new ResourceNode("node" + rnCounter++, loc, item, between(thresholdMin, thresholdMax));
            facilities.add(node);
            locations.add(loc);
        }
        for(double a = minLat; a < maxLat; a += quadSize) {
            for (double b = minLon; b < maxLon; b += quadSize) { // (a,b) = corner of the current quadrant
                int numberOfFacilities = 0;
                if(resourceDensity < 1){
                    if(RNG.nextDouble() < storageDensity) numberOfFacilities = 1;
                }
                else numberOfFacilities = new Float(storageDensity).intValue();
                for(int i = 0; i < numberOfFacilities; i++){
                    Location loc = getUniqueLocationInBounds(locations, world, a, a + quadSize, b, b + quadSize);
                    int resIndex = RNG.nextInt(world.getResources().size());
                    ResourceNode node = new ResourceNode("node" + rnCounter++, loc,
                            world.getResources().get(resIndex), between(thresholdMin, thresholdMax));
                    facilities.add(node);
                    locations.add(loc);
                }
            }
        }

        for(Facility fac: facilities) Log.log(Log.Level.NORMAL, "Created facility: " + fac);

        return facilities;
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
     * Tries to get a new random location within certain bounds
     */
    private Location getRandomLocationInBounds(WorldState world, double minLat, double maxLat, double minLon, double maxLon){
        return world.getMap().getRandomLocationInBounds(new HashSet<>(Collections.singletonList(GraphHopperManager.PERMISSION_ROAD)),
                1000, minLat, maxLat, minLon, maxLon);
    }

    /**
     * Tries to get a new random location that is not already in use
     * @param locations locations that are already in use
     * @param world the world to look for a location in
     */
    private Location getUniqueLocation(Set<Location> locations, WorldState world){
        Location loc = getRandomLocation(world);
        for(int i=0; i<100; i++){
            if(locations.contains(loc)){
                loc = getRandomLocation(world);
                continue;
            }
            return loc;
        }
        return loc;
    }

    /**
     * Tries to get a unique location within certain bounds
     */
    private Location getUniqueLocationInBounds(Set<Location> locations, WorldState world, double minLat, double maxLat, double minLon, double maxLon){
        Location loc = getRandomLocationInBounds(world, minLat, maxLat, minLon, maxLon);
        for(int i=0; i<100; i++){
            if(locations.contains(loc)){
                loc = getRandomLocationInBounds(world, minLat, maxLat, minLon, maxLon);
                continue;
            }
            return loc;
        }
        return loc;
    }

    /**
     * Generates a number of jobs dependent on config parameters
     * @return a set of jobs
     * @param stepNo the number of the current step
     */
    public Set<Job> generateJobs(int stepNo, WorldState world) {
        Set<Job> jobs = new HashSet<>();

        if(RNG.nextDouble() <= jobProbability) jobs.addAll(generateJob(world, stepNo, "regular"));
        if(RNG.nextDouble() <= auctionProbability) jobs.addAll(generateJob(world, stepNo, "auction"));
        if(RNG.nextDouble() <= missionProbability) jobs.addAll(generateJob(world, stepNo, "mission"));

        // Log jobs
        for(Job job: jobs){
            List<String> reqItems = new ArrayList<>();
            for(Item item: job.getRequiredItems().getStoredTypes()){
                reqItems.add(job.getRequiredItems().getItemCount(item) + "x " + item.getName());
            }
            Log.log(Log.Level.NORMAL, "New job: " + job.getName() + ": " + String.join(", ", reqItems) + " reward(" + job.getReward() +
                    ") " + job.getBeginStep() + "-" + job.getEndStep() + " " + job.getStorage() + " " + job.getClass().getSimpleName());
        }

        return jobs;
    }

    private List<Job> generateJob(WorldState world, int step, String type) {
        // draw storage
        List<Storage> storages = world.getStorages();
        Storage storage = storages.get(RNG.nextInt(storages.size()));

        // draw duration
        int duration = between(jobDurationMin, jobDurationMax);

        // draw items
        int reward = 0;
        List<Item> itemsAvailable = world.getAssembledItems();
        ItemBox itemsRequired = new ItemBox();
        int numberOfItems = between(itemCountMin, itemCountMax);
        for(int i = 0; i < numberOfItems; i++) {
            Item item = itemsAvailable.get(RNG.nextInt(itemsAvailable.size()));
            itemsRequired.store(item, 1);
            reward += item.getValue();
        }
        reward *= rewardScale;
        reward += between(rewardModMin, rewardModMax);

        List<Job> result = new ArrayList<>();
        switch(type) {
            case "regular":
                result.add(new Job(reward, storage, step + 1, step + 1 + duration, itemsRequired, JobData.POSTER_SYSTEM));
                break;
            case "auction":
                result.add(new AuctionJob(reward, storage, step + 1, step + 1 + duration, itemsRequired, auctionTime, reward));
                break;
            case "mission":
                String id = "mission" + missionID++;
                for (TeamState team : world.getTeams()) {
                    result.add(new Mission(reward, storage, step + 1, step + 1 + duration, reward, itemsRequired, team, id));
                }
                break;
        }
        return result;
    }

    /**
     * @return the well types which are creatable in the current simulation run
     */
    public Map<String,WellType> generateWellTypes() {
        Map<String, WellType> result = new HashMap<>();
        int wellTypes = between(wellTypesMin, wellTypesMax);
        int efficiency = between(baseEfficiencyMin, baseEfficiencyMax);
        for(int i = 0; i < wellTypes; i++) {
            String name = "wellType" + i;
            efficiency += between(efficiencyIncreaseMin, efficiencyIncreaseMax);
            int integrity = between(baseIntegrityMin, baseIntegrityMax);
            int cost = (int) (costFactor * (efficiency + Math.sqrt(efficiency)));
            WellType type = new WellType(name, Math.max(integrity/2, 1), Math.max(integrity, 1), cost, efficiency);
            result.put(name, type);
            Log.log(Log.Level.NORMAL, String.format("%s: eff(%d), int(%d), cost(%d)", name, efficiency, integrity, cost));
        }
        return result;
    }

    /**
     * @param min min value
     * @param max max value
     * @return random int between min and max values (both bounds inclusive)
     */
    private int between(int min, int max) {
        return min + RNG.nextInt(1 + max - min);
    }
}
