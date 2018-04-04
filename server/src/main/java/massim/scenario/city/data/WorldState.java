package massim.scenario.city.data;

import massim.protocol.scenario.city.data.RoleData;
import massim.util.Log;
import massim.util.RNG;
import massim.config.TeamConfig;
import massim.scenario.city.CityMap;
import massim.scenario.city.data.facilities.*;
import massim.scenario.city.util.Generator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * State of the world.
 */
public class WorldState {

    private CityMap cityMap;

    private int totalSteps;
    private String mapName;
    private long seedCapital;
    private int randomFail;
    private String id;
    private int gotoCost;
    private double rechargeRate;
    private double minLon;
    private double maxLon;
    private double minLat;
    private double maxLat;

    private Map<String, Item> items = new HashMap<>();
    private List<Item> assembledItems = new ArrayList<>();
    private List<Item> resources = new ArrayList<>();

    private Map<String, Role> roles = new HashMap<>();

    private Map<String, Facility> facilities = new HashMap<>();
    private List<Workshop> workshops = new ArrayList<>();
    private List<Dump> dumps = new ArrayList<>();
    private List<ChargingStation> chargingStations = new ArrayList<>();
    private List<Shop> shops = new ArrayList<>();
    private List<Storage> storages = new ArrayList<>();
    private List<ResourceNode> resourceNodes = new ArrayList<>();
    private Set<Well> wells = new HashSet<>();

    private Vector<String> agentNames;
    private Map<String, String> agentToTeam = new HashMap<>();
    private Map<String, Entity> agentToEntity = new HashMap<>();
    private Map<Entity, String> entityToAgent = new HashMap<>();
    private Map<Location, Facility> facilityByLocation = new HashMap<>();
    private Map<String, TeamState> teams = new HashMap<>();
    private Map<String, Job> jobs = new HashMap<>();
    private List<Job> newJobs = new Vector<>();

    private Generator gen;

    private Map<String, WellType> wellTypes;
    private Set<Integer> wellNumbers = new HashSet<>();

    private Map<String, Upgrade> upgrades = new HashMap<>();

    public WorldState(int steps, JSONObject config, Set<TeamConfig> matchTeams, Generator generator) {

        gen = generator;
        totalSteps = steps;

        // parse simulation config
        id = config.optString("id", "Default-simulation");
        Log.log(Log.Level.NORMAL, "Configuring simulation id: " + id);
        mapName = config.optString("map", "london");
        Log.log(Log.Level.NORMAL, "Configuring scenario map: " + mapName);
        seedCapital = config.optLong("seedCapital", 50000L);
        Log.log(Log.Level.NORMAL, "Configuring scenario seedCapital: " + seedCapital);
        minLon = config.optDouble("minLon", 0);
        Log.log(Log.Level.NORMAL, "Configuring scenario minLon: " + minLon);
        maxLon = config.optDouble("maxLon", 0);
        Log.log(Log.Level.NORMAL, "Configuring scenario maxLon: " + maxLon);
        minLat = config.optDouble("minLat", 0);
        Log.log(Log.Level.NORMAL, "Configuring scenario minLat: " + minLat);
        maxLat = config.optDouble("maxLat", 0);
        Log.log(Log.Level.NORMAL, "Configuring scenario maxLat: " + maxLat);
        int proximity = config.optInt("proximity", 4);
        Log.log(Log.Level.NORMAL, "Configuring scenario proximity: " + proximity);
        Location.setProximity(proximity);
        int cellSize = config.optInt("cellSize", 500);
        Log.log(Log.Level.NORMAL, "Configuring scenario cellSize: " + cellSize);
        double centerLat = config.optDouble("centerLat", 0);
        Log.log(Log.Level.NORMAL, "Configuring scenario centerLat: " + centerLat);
        double centerLon = config.optDouble("centerLon", 0);
        Log.log(Log.Level.NORMAL, "Configuring scenario centerLon: " + centerLon);
        Location mapCenter = new Location(centerLon, centerLat);
        randomFail = config.optInt("randomFail", 1);
        Log.log(Log.Level.NORMAL, "Configuring random fail probability: " + randomFail);
        gotoCost = config.optInt("gotoCost", 10);
        Log.log(Log.Level.NORMAL, "Configuring cost for goto: " + gotoCost);
        rechargeRate = config.optDouble("rechargeRate", 0.3);
        Log.log(Log.Level.NORMAL, "Configuring recharge rate: " + rechargeRate);

        // parse upgrades
        JSONArray confUpgrades = config.getJSONArray("upgrades");
        for(int i = 0; i < confUpgrades.length(); i++) {
            JSONObject confUpgrade = confUpgrades.getJSONObject(i);
            String name = confUpgrade.optString("name", "default");
            upgrades.put(name, new Upgrade(name, confUpgrade.optInt("cost", 1000), confUpgrade.optInt("step", 1)));
        }

        parseRoles(config.optJSONObject("roles"));

        cityMap = new CityMap(mapName, cellSize, minLat, maxLat, minLon, maxLon, mapCenter);

        // store teams
        matchTeams.forEach(team -> {
            Vector<String> agNames = team.getAgentNames();
            agNames.forEach(agName -> agentToTeam.put(agName, team.getName()));
            teams.put(team.getName(), new TeamState(seedCapital, team.getName()));
        });
        agentNames = new Vector<>(agentToTeam.keySet());

        // check entity roles
        Vector<String> roleSequence = new Vector<>();
        JSONArray entities = config.optJSONArray("entities");
        if(entities != null){
            for (int i = 0; i < entities.length(); i++) {
                JSONObject entityConf = entities.optJSONObject(i);
                if (entityConf != null){
                    String roleName = entityConf.keys().next();
                    int amount = entityConf.optInt(roleName, 0);
                    for (int j = 0; j < amount; j++){
                        roleSequence.add(roleName);
                    }
                }
            }
        }

        // generate the things
        generator.generateItems(new ArrayList<>(roles.values())).forEach(i -> items.put(i.getName(), i));
        for (Item item : items.values()) {
            if(item.needsAssembly()) assembledItems.add(item);
            else resources.add(item);
        }
        generator.generateFacilities(this).forEach(f -> facilities.put(f.getName(), f));
        facilities.values().forEach(f -> facilityByLocation.put(f.getLocation(), f));
        facilities.values().forEach(f -> {
            if(f instanceof Workshop) workshops.add((Workshop) f);
            else if(f instanceof ChargingStation) chargingStations.add((ChargingStation) f);
            else if(f instanceof Shop) shops.add((Shop) f);
            else if(f instanceof Storage) storages.add((Storage) f);
            else if(f instanceof Dump) dumps.add((Dump) f);
            else if(f instanceof ResourceNode) resourceNodes.add((ResourceNode) f);
        });
        wellTypes = generator.generateWellTypes();

        // draw initial locations
        Location[] initialLocations = new Location[roleSequence.size()];
        Set<String> roads = new HashSet<>(Collections.singletonList("roads"));
        for (int i = 0; i < initialLocations.length; i++) {
            initialLocations[i] = cityMap.getRandomLocation(roads, 1000);
        }

        // create entities and map to agents
        matchTeams.forEach(team -> {
            for (int i = 0; i < roleSequence.size(); i++) {
                Entity e = new Entity(roles.get(roleSequence.get(i)), initialLocations[i]);
                String agentName;
                if(team.getAgentNames().size() > i) {
                    agentName = team.getAgentNames().get(i);
                }
                else {
                    agentName = team.getName() + "-unconfigured-" + i;
                    Log.log(Log.Level.ERROR, "Too few agents configured for team " + team.getName()
                                              + ", using agent name " + agentName + ".");
                }
                agentToEntity.put(agentName, e);
                entityToAgent.put(e, agentName);
            }
        });
    }

    /**
     * Reads role information from the "role" JSON object.
     * @param roles the JSON object hopefully containing some roles
     */
    private void parseRoles(JSONObject roles) {
        if (roles == null){
            Log.log(Log.Level.CRITICAL, "No roles defined");
            return;
        }
        roles.keys().forEachRemaining(roleName -> {
            JSONObject roleJson = roles.optJSONObject(roleName);
            if (roleJson == null) {
                Log.log(Log.Level.ERROR, "Invalid JSON role object.");
            }
            else {
                JSONArray permissionsJson = roleJson.optJSONArray("roads");
                Set<String> permissions = new HashSet<>();
                if (permissionsJson != null){
                    for (int i = 0; i < permissionsJson.length(); i++) {
                        permissions.add(permissionsJson.optString(i, ""));
                    }
                }
                this.roles.put(roleName, new Role(
                        new RoleData(
                                roleName,
                                roleJson.optInt("baseSpeed", 3),
                                roleJson.optInt("maxSpeed", 6),
                                roleJson.optInt("baseBattery", 100),
                                roleJson.optInt("maxBattery", 1000),
                                roleJson.optInt("baseLoad", 100),
                                roleJson.optInt("maxLoad", 1000),
                                roleJson.optInt("baseSkill", 1),
                                roleJson.optInt("maxSkill", 5),
                                roleJson.optInt("baseVision", 500),
                                roleJson.optInt("maxVision", 2000)
                        ),
                        permissions));
            }
        });
    }

    /**
     * @return a new list of all agents in no particular order
     */
    public List<String> getAgents() {
        return new ArrayList<>(agentNames);
    }

    /**
     * @param agentName the name of an agent
     * @return the entity connected to that agent or null if no such entity exists
     */
    public Entity getEntity(String agentName) {
        return agentToEntity.get(agentName);
    }

    /**
     * @return the total number of steps of the simulation
     */
    public int getSteps() {
        return totalSteps;
    }

    /**
     * @param agentName the name of an agent
     * @return the name of the agent's team or null if no such agent exists
     */
    public String getTeamForAgent(String agentName) {
        return agentToTeam.get(agentName);
    }

    /**
     * @return the name of the map used in this simulation
     */
    public String getMapName() {
        return mapName;
    }

    /**
     * @return the seed capital for this simulation
     */
    public long getSeedCapital() {
        return seedCapital;
    }

    /**
     * @return a new list containing all items of this simulation (excl. tools)
     */
    public List<Item> getItems() {
        return new ArrayList<>(items.values());
    }

    /**
     * @param name the name of the facility
     * @return the facility with the given name or null if it does not exist
     */
    public Facility getFacility(String name) {
        return facilities.get(name);
    }

    /**
     * @return the map of this world
     */
    public CityMap getMap(){
        return cityMap;
    }

    /**
     * @return a set containing all entities in the simulation
     */
    public Set<Entity> getEntities() {
        return new HashSet<>(entityToAgent.keySet());
    }

    /**
     * @param loc a location
     * @return the facility at the given location or null, if there is none
     */
    public Facility getFacilityByLocation(Location loc){
        return facilityByLocation.get(loc);
    }

    /**
     * Retrieves a team.
     * @param name name of the team
     * @return the state of the team or null if no team with that name exists
     */
    public TeamState getTeam(String name){
        return teams.get(name);
    }

    /**
     * @param name name of a job
     * @return the job with the given name or null if no such job exists
     */
    public Job getJob(String name) {
        return jobs.get(name);
    }

    /**
     * Adds the given job to the system (as a new job).
     * The job will not show up until the end of the simulation step.
     */
    public void addJob(Job job){
        newJobs.add(job);
    }

    /**
     * Lets the jobs get names and transfers them to the global job list.
     * This is to ensure that the name of the job does not indicate who posted it.
     */
    public void processNewJobs() {
        RNG.shuffle(newJobs);
        newJobs.forEach(job -> {
            job.acquireName();
            jobs.put(job.getName(), job);
        });
        newJobs.clear();
    }

    /**
     * @return the probability for any action to be random_fail
     */
    public int getRandomFail(){
        return randomFail;
    }

    public String getSimID() {
        return id;
    }

    /**
     * @param e an entity in this world
     * @return the name of the agent connected to the entity or null if this entity does not exist
     */
    public String getAgentForEntity(Entity e){
        return entityToAgent.get(e);
    }

    public List<Dump> getDumps() {
        return dumps;
    }

    public List<Workshop> getWorkshops() {
        return workshops;
    }

    public List<ChargingStation> getChargingStations() {
        return chargingStations;
    }

    public List<Shop> getShops() {
        return shops;
    }

    public List<Storage> getStorages() {
        return storages;
    }

    public List<ResourceNode> getResourceNodes() { return resourceNodes; }

    public Set<Well> getWells() { return wells; }

    /**
     * @return a new list of all team states
     */
    public List<TeamState> getTeams() {
        return new ArrayList<>(teams.values());
    }

    /**
     * @return a new set of all jobs regardless of their state
     */
    public Set<Job> getJobs() {
        return new HashSet<>(jobs.values());
    }

    /**
     * @return a new list containing all roles in this simulation
     */
    public List<Role> getRoles(){
        return new Vector<>(roles.values());
    }

    /**
     * @return a list of all facilities in this world
     */
    public List<Facility> getFacilities(){
        return new ArrayList<>(facilities.values());
    }

    /**
     * @return the probability to restore 1 energy (0 to 1)
     */
    public double getRechargeRate(){
        return rechargeRate;
    }

    /**
     * @return the energy cost for the goto action
     */
    public int getGotoCost(){
        return gotoCost;
    }

    /**
     * @return minLon
     */
    public double getMinLon(){ return minLon; }

    /**
     * @return maxLon
     */
    public double getMaxLon(){ return maxLon; }

    /**
     * @return minLat
     */
    public double getMinLat(){ return minLat; }

    /**
     * @return maxLat
     */
    public double getMaxLat(){ return maxLat; }

    /**
     * @return the generator for this world
     */
    public Generator getGenerator(){ return gen;}

    /**
     * Retrieves the well type of the given name.
     * @param typeName name of the well type
     * @return the requested well type or null if no such type exists
     */
    public WellType getWellType(String typeName) {
        return wellTypes.get(typeName);
    }

    /**
     * Creates a well for the given agent.
     * @param wellType the type of the well
     * @param agent name of the agent that created the well
     */
    public void addWell(WellType wellType, String agent) {
        int wellNumber;
        while(true){
            wellNumber = RNG.nextInt(10000);
            if(!wellNumbers.contains(wellNumber)) break;
        }
        wellNumbers.add(wellNumber);
        Well well = new Well("well" + wellNumber, getTeamForAgent(agent), getEntity(agent).getLocation(), wellType);
        facilities.put(well.getName(), well);
        facilityByLocation.put(well.getLocation(), well);
        wells.add(well);
    }

    /**
     * Completely removes a given well.
     * @param w the facility to remove
     */
    public void removeWell(Well w) {
        facilities.remove(w.getName());
        facilityByLocation.remove(w.getLocation());
        wells.remove(w);
    }

    /**
     * @return the original list of assembled items
     */
    public List<Item> getAssembledItems() {
        return assembledItems;
    }

    /**
     * @param name name of an item
     * @return the item by that name or null
     */
    public Item getItemByName(String name) {
        return items.get(name);
    }

    /**
     * @return the original list of resources
     */
    public List<Item> getResources() {
        return resources;
    }

    /**
     * @param ability the ability to get an upgrade for
     * @return the upgrade for that ability
     */
    public Upgrade getUpgrade(String ability) {
        return upgrades.get(ability);
    }

    public Set<WellType> getWellTypes() {
        return new HashSet<>(wellTypes.values());
    }

    public List<Upgrade> getUpgrades() {
        return new ArrayList<>(upgrades.values());
    }
}

