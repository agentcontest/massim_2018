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
    private double blackoutProbability;
    private int blackoutTimeMin;
    private int blackoutTimeMax;

    private double chargingDensity;
    private int rateMin;
    private int rateMax;

    // shop parameters
    private double shopDensity;
    private int minProd;
    private int maxProd;
    private int amountMin;
    private int amountMax;
    private int priceAddMin;
    private int priceAddMax;
    private int restockMin;
    private int restockMax;

    private double dumpDensity;

    private double workshopDensity;

    private double storageDensity;
    private int capacityMin;
    private int capacityMax;

    private double resourceDensity;
    private int gatherFrequencyMin;
    private int gatherFrequencyMax;

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
    private double rate;
    private double auctionProbability;
    private double missionProbability;
    private int productTypesMin;
    private int productTypesMax;
    private int difficultyMin;
    private int difficultyMax;
    private int timeMin;
    private int timeMax;
    private int rewardAddMin;
    private int rewardAddMax;

    private int auctionTimeMin;
    private int auctionTimeMax;
    private int fineSub;
    private int fineAdd;
    private int maxRewardAdd;

    private int missionDifficultyMax;

    private int missionID = 0;
    private int missionEnd = 0;

    private Set<Facility> blackoutFacilities = new HashSet<>();

    public Generator(JSONObject randomConf){
        //parse facilities
        JSONObject facilities = randomConf.optJSONObject("facilities");
        if(facilities == null) {
            Log.log(Log.Level.ERROR, "No facilities in configuration.");
        } else {
            quadSize = facilities.optDouble("quadSize", 0.4);
            Log.log(Log.Level.NORMAL, "Configuring facilities quadSize: " + quadSize);
            blackoutProbability = facilities.optDouble("blackoutProbability", 0.1);
            Log.log(Log.Level.NORMAL, "Configuring facilities blackoutProbability: " + blackoutProbability);
            blackoutTimeMin = facilities.optInt("blackoutTimeMin", 5);
            Log.log(Log.Level.NORMAL, "Configuring facilities blackoutTimeMin: " + blackoutTimeMin);
            blackoutTimeMax = facilities.optInt("blackoutTimeMax", 10);
            Log.log(Log.Level.NORMAL, "Configuring facilities blackoutTimeMax: " + blackoutTimeMax);

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
                minProd = shops.optInt("minProd", 3);
                Log.log(Log.Level.NORMAL, "Configuring facilities shop minProd: " + minProd);
                maxProd = shops.optInt("maxProd", 10);
                Log.log(Log.Level.NORMAL, "Configuring facilities shop maxProd: " + maxProd);
                amountMin = shops.optInt("amountMin", 5);
                Log.log(Log.Level.NORMAL, "Configuring facilities shop amountMin: " + amountMin);
                amountMax = shops.optInt("amountMax", 20);
                Log.log(Log.Level.NORMAL, "Configuring facilities shop amountMax: " + amountMax);
                priceAddMin = shops.optInt("priceAddMin", 100);
                Log.log(Log.Level.NORMAL, "Configuring facilities shop priceAddMin: " + priceAddMin);
                priceAddMax = shops.optInt("priceAddMax", 150);
                Log.log(Log.Level.NORMAL, "Configuring facilities shop priceAddMax: " + priceAddMax);
                restockMin = shops.optInt("restockMin", 1);
                Log.log(Log.Level.NORMAL, "Configuring facilities shop restockMin: " + restockMin);
                restockMax = shops.optInt("restockMax", 5);
                Log.log(Log.Level.NORMAL, "Configuring facilities shop restockMax: " + restockMax);
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
                gatherFrequencyMin = resourceNodes.optInt("gatherFrequencyMin", 4);
                Log.log(Log.Level.NORMAL, "Configuring facilities resource node gatherFrequencyMin: " + gatherFrequencyMin);
                gatherFrequencyMax = resourceNodes.optInt("gatherFrequencyMax", 8);
                Log.log(Log.Level.NORMAL, "Configuring facilities resource node gatherFrequencyMax: " + gatherFrequencyMax);
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
            rate = jobs.optDouble("rate", 0.2);
            Log.log(Log.Level.NORMAL, "Configuring jobs rate: " + rate);
            auctionProbability = jobs.optDouble("auctionProbability", 0.4);
            Log.log(Log.Level.NORMAL, "Configuring jobs auctionProbability: " + auctionProbability);
            missionProbability = jobs.optDouble("missionProbability", 0.1);
            Log.log(Log.Level.NORMAL, "Configuring jobs missionProbability: " + missionProbability);
            productTypesMin = jobs.optInt("productTypesMin", 1);
            Log.log(Log.Level.NORMAL, "Configuring jobs productTypesMin: " + productTypesMin);
            productTypesMax = jobs.optInt("productTypesMax", 4);
            Log.log(Log.Level.NORMAL, "Configuring jobs productTypesMax: " + productTypesMax);
            difficultyMin = jobs.optInt("difficultyMin", 3);
            Log.log(Log.Level.NORMAL, "Configuring jobs difficultyMin: " + difficultyMin);
            difficultyMax = jobs.optInt("difficultyMax", 12);
            Log.log(Log.Level.NORMAL, "Configuring jobs difficultyMax: " + difficultyMax);
            timeMin = jobs.optInt("timeMin", 100);
            Log.log(Log.Level.NORMAL, "Configuring jobs timeMin: " + timeMin);
            timeMax = jobs.optInt("timeMax", 400);
            Log.log(Log.Level.NORMAL, "Configuring jobs timeMax: " + timeMax);
            rewardAddMin = jobs.optInt("rewardAddMin", 50);
            Log.log(Log.Level.NORMAL, "Configuring jobs rewardAddMin: " + rewardAddMin);
            rewardAddMax = jobs.optInt("rewardAddMax", 100);
            Log.log(Log.Level.NORMAL, "Configuring jobs rewardAddMax: " + rewardAddMax);


            //parse auctions
            JSONObject auctions = jobs.optJSONObject("auctions");
            if (auctions == null) {
                Log.log(Log.Level.ERROR, "No auctions in configuration.");
            } else {
                auctionTimeMin = auctions.optInt("auctionTimeMin", 2);
                Log.log(Log.Level.NORMAL, "Configuring jobs auctionTimeMin: " + auctionTimeMin);
                auctionTimeMax = auctions.optInt("auctionTimeMax", 10);
                Log.log(Log.Level.NORMAL, "Configuring jobs auctionTimeMax: " + auctionTimeMax);
                fineSub = auctions.optInt("fineSub", 50);
                Log.log(Log.Level.NORMAL, "Configuring jobs fineSub: " + fineSub);
                fineAdd = auctions.optInt("fineAdd", 50);
                Log.log(Log.Level.NORMAL, "Configuring jobs fineAdd: " + fineAdd);
                maxRewardAdd = auctions.optInt("maxRewardAdd", 50);
                Log.log(Log.Level.NORMAL, "Configuring jobs maxRewardAdd: " + maxRewardAdd);
            }

            //parse missions
            JSONObject missions = jobs.optJSONObject("missions");
            if (missions == null) {
                Log.log(Log.Level.ERROR, "No missions in configuration.");
            } else {
                missionDifficultyMax = missions.optInt("missionDifficultyMax", 2);
                Log.log(Log.Level.NORMAL, "Configuring jobs missionDifficultyMax: " + missionDifficultyMax);
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
        for(int i = 1; i <= layers; i++){
            levelAmount = Math.max(1, levelAmount - between(levelDecreaseMin, levelDecreaseMax));
            List<Item> layerItems = new ArrayList<>();
            for(int j = 1; j <= levelAmount; j++){
                // draw random parts from all items on previous layers
                int numberOfParts = between(partsMin, partsMax);
                List<Item> possibleParts = new ArrayList<>(items);
                Collections.shuffle(possibleParts);
                Set<Item> parts = new HashSet<>(possibleParts.subList(0, Math.min(numberOfParts, possibleParts.size())));
                int volume = parts.stream().mapToInt(Item::getVolume).sum();

                // determine required roles
                Collections.shuffle(roles);
                Set<Role> requiredRoles = new HashSet<>(roles.subList(0, 2));

                Item item = new Item("item" + items.size(), volume, parts, requiredRoles);
                layerItems.add(item);
            }
            items.addAll(layerItems);
        }
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
        List<ResourceNode> resourceNodes = new ArrayList<>();
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

        // generate empty shops
        int shopCounter = 0;
        for (double a = minLat; a < maxLat; a += quadSize) {
            for (double b = minLon; b < maxLon; b += quadSize) { // (a,b) = corner of the current quadrant

                int numberOfFacilities = 0;
                if(shopDensity < 1){
                    if(RNG.nextDouble() < shopDensity){
                        numberOfFacilities = 1;
                    }
                }
                else{
                    numberOfFacilities = new Float(shopDensity).intValue();
                }
                for(int i = 0; i < numberOfFacilities; i++){
                    Location loc = getUniqueLocationInBounds(locations, world, a, a + quadSize, b, b + quadSize);
                    Shop shop = new Shop("shop" + shopCounter, loc,RNG.nextInt(restockMax - restockMin + 1) + restockMin);
                    facilities.add(shop);
                    locations.add(shop.getLocation());
                    shops.add(shop);
                    shopCounter++;
                }
            }
        }
        if(shopCounter == 0){
            Shop shop = new Shop("shop" + shopCounter, getUniqueLocation(locations, world),
                    RNG.nextInt(restockMax - restockMin + 1) + restockMin);
            facilities.add(shop);
            locations.add(shop.getLocation());
            shops.add(shop);
        }
        // TODO generate new shop params (and overhaul shops)

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
        int nodeCounter = 0;
        // TODO generate lots of resource nodes

        for(ResourceNode node: resourceNodes){
            Log.log(Log.Level.NORMAL, "Added resource node: " + node.getName() + ": " + node.getResource().getName() +
                    " " + node.getLocation().getLat() + ", " + node.getLocation().getLon());
        }

        for(Facility fac: facilities) Log.log(Log.Level.NORMAL, "Added facility: " + fac);

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
     * Randomly picks a number of items to use for a job
     * @param possibleItems list of all items that can be used for the job
     */
    private Map<Item, Integer> determineJobItems(List<Item> possibleItems, boolean mission){

        Map<Item, Integer> result = new HashMap<>();
        int currentDifficulty = 0;
        int difficulty = RNG.nextInt((mission? missionDifficultyMax : difficultyMax) - difficultyMin + 1) + difficultyMin;

        int numberOfItems = Math.min(RNG.nextInt(productTypesMax - productTypesMin + 1) + productTypesMin,
                possibleItems.size());

        // TODO make a job according to new rules

        // safeguard
        if(result.isEmpty()) result.put(possibleItems.get(0), 1);

        return result;
    }

    /**
     * Generates a number of jobs dependent on config parameters
     * @return a set of jobs
     * @param stepNo the number of the current step
     */
    public Set<Job> generateJobs(int stepNo, WorldState world) {
        Set<Job> jobs = new HashSet<>();

        double jobProb = Math.exp(-1d * (double)stepNo/(double)world.getSteps()) * rate;
        if(RNG.nextDouble() <= jobProb){ // create a new job

            boolean createMission = stepNo >= missionEnd && RNG.nextDouble() <= missionProbability;

            List<Storage> storageList = new ArrayList<>(world.getStorages());
            Storage storage = storageList.get(RNG.nextInt(storageList.size()));
            Map<Item, Integer> jobItems = determineJobItems(new ArrayList<>(world.getAssembledItems()), createMission);
            int length = RNG.nextInt(timeMax - timeMin + 1) + timeMin;

            int reward = computeReward(jobItems);
            int rewardAdd = (int) (reward*(RNG.nextInt((rewardAddMax - rewardAddMin) + 1) + rewardAddMin)/100.0f);
            reward += rewardAdd;
            if (difficultyMin == 0 && difficultyMax == 0 && missionDifficultyMax == 0) reward = jobItems.keySet().size() * 100;

            if(!createMission){

                if (difficultyMin == 0 && difficultyMax == 0 && missionDifficultyMax == 0) {
                    reward = jobItems.keySet().size() * 100;
                }

                Job job;
                if(RNG.nextDouble() <= auctionProbability){
                    // create auction
                    int auctionTime = RNG.nextInt(auctionTimeMax - auctionTimeMin + 1) + auctionTimeMin;
                    int fine;
                    int fineMod = 1 + RNG.nextInt(fineAdd + fineSub);
                    if(fineMod > fineSub) {
                        fine = reward + (int) (reward * ((fineMod - fineSub) / 100.0f));
                    } else {
                        fine = reward - (int) (reward * (fineMod / 100.0f));
                    }
                    maxRewardAdd = 1 + RNG.nextInt(rewardAddMax);
                    reward += (int) (reward * maxRewardAdd / 100.0f);
                    job = new AuctionJob(reward, storage, stepNo + 1, stepNo + 1 + length, auctionTime, fine);
                }
                else {
                    // create regular job
                    job = new Job(reward, storage, stepNo + 1, stepNo + 1 + length, JobData.POSTER_SYSTEM);
                }
                for(Item item: jobItems.keySet()) job.addRequiredItem(item, jobItems.get(item));
                jobs.add(job);
            } else {
                // create mission
                missionEnd = stepNo + 1 + length;
                int fine;
                int fineMod = 1 + RNG.nextInt(fineAdd + fineSub);
                if(fineMod > fineSub){
                    fine = reward + (int) (reward * ((fineMod - fineSub) / 100.0f));
                } else {
                    fine = reward - (int) (reward * (fineMod / 100.0f));
                }

                for(TeamState team: world.getTeams()){ // one mission instance for each team
                    Mission mission = new Mission(reward, storage, stepNo + 1, stepNo + 1 + length, fine, team, String.valueOf(missionID));
                    for(Item item: jobItems.keySet()) mission.addRequiredItem(item, jobItems.get(item));
                    jobs.add(mission);
                }
                missionID++;
            }
        }

        // Log jobs
        for(Job job: jobs){
            List<String> reqItems = new ArrayList<>();
            for(Item item: job.getRequiredItems().getStoredTypes()){
                reqItems.add(job.getRequiredItems().getItemCount(item) + "x " + item.getName());
            }
            Log.log(Log.Level.NORMAL, "New job: " + job.getName() + ": " + String.join(", ", reqItems) + " " + job.getReward() +
                    " " + job.getBeginStep() + " " + job.getEndStep() + " " + job.getStorage() + " " + job.getClass().getSimpleName());
        }

        return jobs;
    }

    /**
     * @param requiredItems items required to complete the job
     * @return reward for a job with the corresponding required items
     */
    private int computeReward(Map<Item, Integer> requiredItems){
        int reward = 0;
        for (Map.Entry<Item, Integer> entry : requiredItems.entrySet()) {
            reward += entry.getKey().getValue() * entry.getValue() * 10;
        }
        return reward;
    }

    /**
     * Progresses each facility's blackout and may generate new ones.
     * @param world the current world state
     */
    public void handleBlackouts(WorldState world){

        // manage facilities affected by blackout
        List<Facility> workingFacilities = new ArrayList<>();
        for(Facility facility: blackoutFacilities){
            facility.stepBlackoutCounter();
            if(facility.stepBlackoutCounter() == 0) workingFacilities.add(facility);
        }
        blackoutFacilities.removeAll(workingFacilities);

        // initiate new blackout
        if(RNG.nextDouble() < blackoutProbability){
            List<Facility> facilities = new ArrayList<>(world.getChargingStations());
            Facility targetFacility = facilities.get(RNG.nextInt(facilities.size()));
            if(!blackoutFacilities.contains(targetFacility)){
                int duration = RNG.nextInt(blackoutTimeMax - blackoutTimeMin + 1) + blackoutTimeMin;
                targetFacility.initiateBlackout(duration);
                blackoutFacilities.add(targetFacility);
                Log.log(Log.Level.NORMAL, "New blackout in " + targetFacility.getName() + ", duration " + duration + " steps.");
            }
        }
    }

    /**
     * Adds a facility to the list of facilities affected by blackout (for testing)
     */
    public void addToBlackoutFacilities(Facility facility){ blackoutFacilities.add(facility);}

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
