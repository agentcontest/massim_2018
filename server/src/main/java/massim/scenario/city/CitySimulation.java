package massim.scenario.city;

import massim.config.TeamConfig;
import massim.protocol.DynamicWorldData;
import massim.protocol.StaticWorldData;
import massim.protocol.messagecontent.Action;
import massim.protocol.messagecontent.RequestAction;
import massim.protocol.messagecontent.SimEnd;
import massim.protocol.messagecontent.SimStart;
import massim.protocol.scenario.city.data.*;
import massim.protocol.scenario.city.percept.CityInitialPercept;
import massim.protocol.scenario.city.percept.CityStepPercept;
import massim.scenario.AbstractSimulation;
import massim.scenario.city.data.*;
import massim.scenario.city.data.facilities.Facility;
import massim.scenario.city.data.facilities.Shop;
import massim.scenario.city.data.facilities.Storage;
import massim.scenario.city.util.Generator;
import massim.util.Log;
import massim.util.RNG;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main class of the City scenario (2017).
 * @author ta10
 */
public class CitySimulation extends AbstractSimulation {

    private int currentStep = -1;
    private WorldState world;
    private ActionExecutor actionExecutor;
    private Generator generator;
    private StaticCityData staticData;

    @Override
    public Map<String, SimStart> init(int steps, JSONObject config, Set<TeamConfig> matchTeams) {

        // build the random generator
        JSONObject randomConf = config.optJSONObject("generate");
        if(randomConf == null){
            Log.log(Log.Level.ERROR, "No random generation parameters!");
            randomConf = new JSONObject();
        }
        generator = new Generator(randomConf);

        // create the most important things
        world = new WorldState(steps, config, matchTeams, generator);
        actionExecutor = new ActionExecutor(world);

        // create data objects for all items
        List<Item> allItems = world.getItems();
        allItems.addAll(world.getTools());
        List<ItemData> itemData = allItems.stream()
                .map(item -> new ItemData(
                        item.getName(),
                        item.getVolume(),
                        item.getRequiredItems().entrySet().stream()
                                .map(e -> new ItemAmountData(e.getKey().getName(), e.getValue()))
                                .collect(Collectors.toList()),
                        item.getRequiredTools().stream()
                                .map(tool -> new ToolData(tool.getName()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());

        // create the static data object
        staticData = new StaticCityData(world.getSimID(), world.getSteps(), world.getMapName(), world.getSeedCapital(),
                                        world.getTeams().stream()
                                                        .map(TeamState::getName)
                                                        .collect(Collectors.toList()),
                                        world.getRoles().stream()
                                                        .map(Role::getRoleData)
                                                        .collect(Collectors.toList()),
                                        itemData);

        // determine initial percepts
        Map<String, SimStart> initialPercepts = new HashMap<>();
        world.getAgents().forEach(agName -> initialPercepts.put(agName,
                new CityInitialPercept(
                        world.getSimID(),
                        steps,
                        world.getTeamForAgent(agName),
                        world.getMapName(),
                        world.getSeedCapital(),
                        world.getEntity(agName).getRole().getRoleData(),
                        itemData
                        )));
        return initialPercepts;
    }

    @Override
    public Map<String, RequestAction> preStep(int stepNo) {

        currentStep = stepNo;

         // step job generator
        generator.generateJobs(stepNo, world).forEach(job -> world.addJob(job));

        // activate jobs for this step
        world.getJobs().stream()
                .filter(job -> job.getBeginStep() == stepNo)
                .forEach(Job::activate);

        /* create percept data */
        // create team data
        Map<String, TeamData> teamData = new HashMap<>();
        world.getTeams().forEach(team -> teamData.put(team.getName(), new TeamData(null, team.getMoney())));

        // create entity data as visible to other entities (containing name, team, role and location)
        List<EntityData> entities = new Vector<>();
        world.getAgents().forEach(agent -> {
            Entity entity = world.getEntity(agent);
            entities.add(new EntityData(null, null, null, null, null, null,
                    agent, world.getTeamForAgent(agent),
                    entity.getRole().getName(),
                    entity.getLocation().getLat(),
                    entity.getLocation().getLon()));
        });

        // create complete snapshots of entities
        Map<String, EntityData> completeEntities = buildEntityData();

        /* create facility data */
        List<ShopData> shops = buildShopData();
        List<WorkshopData> workshops = buildWorkshopData();
        List<ChargingStationData> stations = buildChargingStationData();
        List<DumpData> dumps = buildDumpData();
        List<ResourceNodeData> resourceNodes = buildResourceNodeData();

        // storage
        Map<String, List<StorageData>> storageMap = new HashMap<>();
        for (TeamState team : world.getTeams()) {
            List<StorageData> storageData = new Vector<>();
            for (Storage storage: world.getStorages()){
                List<StoredData> items = new Vector<>();
                for(Item item: world.getItems()){
                    // add an entry if item is either stored or delivered for the team
                    int stored = storage.getStored(item, team.getName());
                    int delivered = storage.getDelivered(item, team.getName());
                    if(stored > 0 || delivered > 0) items.add(new StoredData(item.getName(), stored, delivered));
                }
                StorageData sd = new StorageData(storage.getName(),
                                                 storage.getLocation().getLat(),
                                                 storage.getLocation().getLon(),
                                                 storage.getCapacity(),
                                                 storage.getFreeSpace(),
                                                 items,
                                                 null);
                storageData.add(sd);
            }
            storageMap.put(team.getName(), storageData);
        }

        /* create job data */
        Map<String, List<JobData>> jobsPerTeam = new HashMap<>();
        Map<String, List<JobData>> postedJobsPerTeam = new HashMap<>();
        Map<String, List<AuctionJobData>> auctionsPerTeam = new HashMap<>();
        Map<String, List<MissionData>> missionsPerTeam = new HashMap<>();
        world.getTeams().forEach(team -> postedJobsPerTeam.put(team.getName(), new ArrayList<>()));
        world.getTeams().forEach(team -> jobsPerTeam.put(team.getName(), new ArrayList<>()));

        // add all regular jobs (either as posted or not)
        for (Job job : world.getJobs()) {
            if(!(job instanceof AuctionJob) && job.isActive()){
                JobData jobData = job.toJobData(false, false);
                for(TeamState team: world.getTeams()){
                    if(job.getPoster().equals(team.getName())){
                        // add to team's posted jobs
                        postedJobsPerTeam.get(team.getName()).add(jobData);
                    }
                    else{
                        // add as regular job
                        jobsPerTeam.get(team.getName()).add(jobData);
                    }
                }
            }
        }

        // list of auction jobs in auctioning state (visible to all)
        List<AuctionJobData> auctioningJobs = world.getJobs().stream()
                .filter(job -> (job instanceof AuctionJob && job.getStatus() == Job.JobStatus.AUCTION ))
                .map(job -> job.toJobData(false, false))
                .map(jobData -> (AuctionJobData)jobData)
                .collect(Collectors.toList());

        // add per team: auctions assigned to that team + missions
        world.getTeams().forEach(team -> {
            List<AuctionJobData> teamJobs = new Vector<>(auctioningJobs);
            List<MissionData> teamMissions = new Vector<>();
            for (Job job : world.getJobs()) {
                if(job instanceof AuctionJob
                        && ((AuctionJob)job).getAuctionWinner().equals(team.getName())
                        && job.isActive()){
                    if(job instanceof Mission) teamMissions.add((MissionData) job.toJobData(false, false));
                    else teamJobs.add((AuctionJobData) job.toJobData(false, false));
                }
            }
            auctionsPerTeam.put(team.getName(), teamJobs);
            missionsPerTeam.put(team.getName(), teamMissions);
        });

        // create and deliver percepts
        Map<String, RequestAction> percepts = new HashMap<>();
        world.getAgents().forEach(agent -> {
            String team = world.getTeamForAgent(agent);
            percepts.put(agent,
                    new CityStepPercept(
                            completeEntities.get(agent),
                            team, stepNo, teamData.get(team), entities, shops, workshops, stations, dumps,
                            storageMap.get(team),
                            resourceNodes,
                            jobsPerTeam,
                            auctionsPerTeam,
                            missionsPerTeam,
                            postedJobsPerTeam,
                            world.getVisibilityRange()
            ));
        });
        return percepts;
    }

    /**
     * Builds dump data objects for all dumps.
     * @return a list of those objects
     */
    private List<DumpData> buildDumpData() {
        return world.getDumps().stream()
                .map(dump -> new DumpData(dump.getName(), dump.getLocation().getLat(), dump.getLocation().getLon()))
                .collect(Collectors.toList());
    }

    /**
     * Builds charging station data objects for all charging stations.
     * @return a list of those objects
     */
    private List<ChargingStationData> buildChargingStationData() {
        return world.getChargingStations().stream()
                .map(cs -> new ChargingStationData(cs.getName(), cs.getLocation().getLat(),
                        cs.getLocation().getLon(), cs.getRate()))
                .collect(Collectors.toList());
    }

    /**
     * Builds workshop data objects for all workshops.
     * @return a list of those objects
     */
    private List<WorkshopData> buildWorkshopData() {
        return world.getWorkshops().stream()
                .map(ws -> new WorkshopData(ws.getName(), ws.getLocation().getLat(), ws.getLocation().getLon()))
                .collect(Collectors.toList());
    }

    /**
     * Builds shop data objects for all shops.
     * @return a list of those objects
     */
    private List<ShopData> buildShopData() {
        return world.getShops().stream()
                .map(shop ->
                        new ShopData(
                                shop.getName(), shop.getLocation().getLat(), shop.getLocation().getLon(),
                                shop.getRestock(),
                                shop.getOfferedItems().stream()
                                        .map(item -> new StockData(item.getName(), shop.getPrice(item), shop.getItemCount(item)))
                                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    /**
     * Builds resource node data objects for all shops.
     * @return a list of those objects
     */
    private List<ResourceNodeData> buildResourceNodeData() {
        return world.getResourceNodes().stream()
                .map(node -> new ResourceNodeData(node.getName(), node.getLocation().getLat(), node.getLocation().getLon(), node.getResource().getName()))
                .collect(Collectors.toList());
    }

    /**
     * Builds an {@link EntityData} object for each entity in the simulation.
     * @return mapping from agent/entity names to the data objects
     */
    private Map<String,EntityData> buildEntityData() {
        Map<String, EntityData> result = new HashMap<>();
        world.getAgents().forEach(agent -> {
            Entity entity = world.getEntity(agent);
            // check if entity is in some facility
            String facilityName = null;
            Facility facility = world.getFacilityByLocation(entity.getLocation());
            if(facility != null) facilityName = facility.getName();
            // check if entity has a route
            List<WayPointData> waypoints = new Vector<>();
            if(entity.getRoute() != null){
                int i = 0;
                for (Location loc: entity.getRoute().getWaypoints()) {
                    waypoints.add(new WayPointData(i++, loc.getLat(), loc.getLon()));
                }
            }
            // create entity snapshot
            result.put(agent,
                    new EntityData(
                            entity.getCurrentBattery(),
                            entity.getCurrentLoad(),
                            new ActionData(entity.getLastAction().getActionType(),
                                    entity.getLastAction().getParameters(),
                                    entity.getLastActionResult()),
                            facilityName,
                            waypoints,
                            entity.getInventory().toItemAmountData(),
                            agent,
                            world.getTeamForAgent(agent),
                            entity.getRole().getName(),
                            entity.getLocation().getLat(),
                            entity.getLocation().getLon()
                    ));
        });
        return result;
    }

    @Override
    public void step(int stepNo, Map<String, Action> actions) {
        // execute all actions in random order
        List<String> agents = world.getAgents();
        RNG.shuffle(agents);
        actionExecutor.preProcess();
        for(String agent: agents)
            actionExecutor.execute(agent, actions, stepNo);
        actionExecutor.postProcess();
        world.getShops().forEach(Shop::step);

        // process new jobs (created in this step)
        world.processNewJobs();

        // tell all jobs which have to end that they have to end
        world.getJobs().stream().filter(job -> job.getEndStep() == stepNo).forEach(Job::terminate);

        // assign auction jobs which have finished auctioning
        world.getJobs().stream()
                .filter(job -> job instanceof AuctionJob
                               && job.getBeginStep() + ((AuctionJob)job).getAuctionTime() - 1 == stepNo
                               && !((AuctionJob)job).isAssigned())
                .forEach(job -> ((AuctionJob)job).assign());
    }

    @Override
    public Map<String, SimEnd> finish() {
        Map<TeamState, Integer> rankings = getRankings();
        Map<String, SimEnd> results = new HashMap<>();
        world.getAgents().forEach(agent -> {
            TeamState team = world.getTeam(world.getTeamForAgent(agent));
            results.put(agent, new SimEnd(rankings.get(team), team.getMoney()));
        });
        return results;
    }

    @Override
    public JSONObject getResult() {
        JSONObject result = new JSONObject();
        Map<TeamState, Integer> rankings = getRankings();
        world.getTeams().forEach(team -> {
            JSONObject teamResult = new JSONObject();
            teamResult.put("score", team.getMoney());
            teamResult.put("ranking", rankings.get(team));
            result.put(team.getName(), teamResult);
        });
        return result;
    }

    /**
     * Calculates the current rankings based on the teams' current money values.
     * @return a map of the current rankings
     */
    private Map<TeamState, Integer> getRankings(){
        Map<TeamState, Integer> rankings = new HashMap<>();
        Map<Long, Set<TeamState>> scoreToTeam = new HashMap<>();
        world.getTeams().forEach(team -> {
            scoreToTeam.putIfAbsent(team.getMoney(), new HashSet<>());
            scoreToTeam.get(team.getMoney()).add(team);
        });
        List<Long> scoreRanking = new ArrayList<>(scoreToTeam.keySet());
        Collections.sort(scoreRanking);     // sort ascending
        Collections.reverse(scoreRanking);  // now descending
        final int[] ranking = {1};
        scoreRanking.forEach(score -> {
            Set<TeamState> teams = scoreToTeam.get(score);
            teams.forEach(team -> rankings.put(team, ranking[0]));
            ranking[0] += teams.size();
        });
        return rankings;
    }

    @Override
    public String getName() {
        return world.getSimID();
    }

    @Override
    public DynamicWorldData getSnapshot() {
        return new DynamicCityData(
                currentStep,
                new ArrayList<>(buildEntityData().values()),
                buildShopData(),
                buildWorkshopData(),
                buildChargingStationData(),
                buildDumpData(),
                buildResourceNodeData(),
                world.getJobs().stream()
                        .map(job -> job.toJobData(true, true))
                        .collect(Collectors.toList()),
                world.getStorages().stream()
                        .map(s -> s.toStorageData(world.getTeams().stream()
                                .map(TeamState::getName)
                                .collect(Collectors.toList())))
                        .collect(Collectors.toList()),
                world.getTeams().stream()
                        .map(team -> new TeamData(team.getName(), team.getMoney()))
                        .collect(Collectors.toList()));
    }

    @Override
    public StaticWorldData getStaticData() {
        return staticData;
    }

    /**
     * Retrieves the simulation state. This is not a replica. Handle with care!!
     * @return the simulation's world state
     */
    WorldState getWorldState(){
        return world;
    }

    @Override
    public void handleCommand(String[] command) {
        switch (command[0]){
            case "give": // "give item0 agentA1 1"
                if(command.length == 4){
                    Item item = world.getItemOrTool(command[1]);
                    Entity agent = world.getEntity(command[2]);
                    int amount = -1;
                    try{amount = Integer.parseInt(command[3]);} catch (NumberFormatException ignored){}
                    if(item != null && agent != null && amount > 0 ){
                        if(agent.addItem(item, amount)){
                            Log.log(Log.Level.NORMAL,
                                    "Added " + amount + " of item " + command[1] + " to agent " + command[2]);
                        }
                        break;
                    }
                }
                Log.log(Log.Level.ERROR, "Invalid give command parameters.");
                break;
            case "store": // "store storage0 item0 A 1"
                if(command.length == 5){
                    Facility facility = world.getFacility(command[1]);
                    Item item = world.getItemOrTool(command[2]);
                    int amount = -1;
                    try{amount = Integer.parseInt(command[4]);} catch (NumberFormatException ignored){}
                    if(facility instanceof Storage && item != null && amount > 0){
                        if(((Storage) facility).store(item, amount, command[3])){
                            Log.log(Log.Level.NORMAL, "Stored items in " + facility.getName());
                        }
                        break;
                    }
                }
                Log.log(Log.Level.ERROR, "Invalid store command parameters.");
                break;
            case "addJob": // "addJob 1 2 100 storage0 item0 1 item1 1 ..."
                if(command.length >= 7 && command.length % 2 == 1){
                    int start = -1;
                    try{start = Integer.parseInt(command[1]);} catch (NumberFormatException ignored){}
                    int end = -1;
                    try{end = Integer.parseInt(command[2]);} catch (NumberFormatException ignored){}
                    int reward = -1;
                    try{reward = Integer.parseInt(command[3]);} catch (NumberFormatException ignored){}
                    Facility facility = world.getFacility(command[4]);
                    Map<Item, Integer> requirements = new HashMap<>();
                    for(int i = 5; i < command.length; i += 2){
                        Item item = world.getItemOrTool(command[i]);
                        int amount = -1;
                        try{amount = Integer.parseInt(command[i+1]);} catch (NumberFormatException ignored){}
                        if(item != null && amount > 0) requirements.put(item, amount);
                    }
                    if(start > 0 && end >= start && reward > 0 && requirements.size() > 0 && facility instanceof Storage) {
                        Job job = new Job(reward, (Storage) facility, start, end, JobData.POSTER_SYSTEM);
                        requirements.forEach(job::addRequiredItem);
                        world.addJob(job);
                        break;
                    }
                }
                Log.log(Log.Level.ERROR, "Invalid addJob command parameters.");
                break;
            case "addAuction": // "addAuction 1 2 100 5 1000 storage0 item0 1 item1 1 ..."
                if(command.length >= 9 && command.length % 2 == 1){
                    int start = -1;
                    try{start = Integer.parseInt(command[1]);} catch (NumberFormatException ignored){}
                    int end = -1;
                    try{end = Integer.parseInt(command[2]);} catch (NumberFormatException ignored){}
                    int reward = -1;
                    try{reward = Integer.parseInt(command[3]);} catch (NumberFormatException ignored){}
                    int auctionTime = -1;
                    try{auctionTime = Integer.parseInt(command[4]);} catch (NumberFormatException ignored){}
                    int fine = -1;
                    try{fine = Integer.parseInt(command[5]);} catch (NumberFormatException ignored){}
                    Facility facility = world.getFacility(command[6]);
                    Map<Item, Integer> requirements = new HashMap<>();
                    for(int i = 7; i < command.length; i += 2){
                        Item item = world.getItemOrTool(command[i]);
                        int amount = -1;
                        try{amount = Integer.parseInt(command[i+1]);} catch (NumberFormatException ignored){}
                        if(item != null && amount > 0) requirements.put(item, amount);
                    }
                    if(start > 0 && end >= start && reward > 0 && requirements.size() > 0 && facility instanceof Storage) {
                        AuctionJob auction = new AuctionJob(reward, (Storage) facility, start, end, auctionTime, fine);
                        requirements.forEach(auction::addRequiredItem);
                        world.addJob(auction);
                        break;
                    }
                }
                Log.log(Log.Level.ERROR, "Invalid addAuction command parameters.");
                break;
            case "print":
                if(command.length > 1) {
                    switch (command[1]) {
                        case "facilities":
                        case "facs":
                            world.getFacilities().forEach(f -> Log.log(Log.Level.NORMAL, f.toString()));
                            break;
                        case "items":
                            world.getItems().forEach(i -> Log.log(Log.Level.NORMAL, i.toString()));
                            break;
                        default:
                            Log.log(Log.Level.ERROR, "Invalid print command argument.");
                    }
                }
                else{
                    Log.log(Log.Level.ERROR, "Invalid print command.");
                }
                break;
        }
    }
}
