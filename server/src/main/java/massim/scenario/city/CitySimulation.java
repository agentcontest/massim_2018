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

    private WorldState world;
    private ActionExecutor actionExecutor;
    private Generator generator;

    private StaticCityData staticData;
    private DynamicCityData lastSnapShot;

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
        List<ItemData> itemData = world.getItems().stream()
                .map(item -> new ItemData(
                        item.getName(),
                        item.getVolume(),
                        item.getRequiredItems().entrySet().stream()
                                .map(e -> new ItemAmountData(e.getKey().getName(), e.getValue()))
                                .collect(Collectors.toList()),
                        item.getRequiredTools().stream()
                                .map(Item::getName)
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

         // step job generator
        generator.generateJobs(stepNo, world).forEach(job -> world.addJob(job));

        // activate jobs for this step
        world.getJobs().stream()
                .filter(job -> job.getBeginStep() == stepNo)
                .forEach(Job::activate);

        /* create percept data */
        // create team data
        Map<String, TeamData> teamData = new HashMap<>();
        world.getTeams().forEach(team -> teamData.put(team.getName(), new TeamData(team.getMoney())));

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
        Map<String, EntityData> completeEntities = new HashMap<>();
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
            completeEntities.put(agent,
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

        // create facility data
        // shops
        List<ShopData> shops = world.getShops().stream()
                .map(shop ->
                    new ShopData(
                        shop.getName(), shop.getLocation().getLat(), shop.getLocation().getLon(),
                        shop.getRestock(),
                        shop.getOfferedItems().stream()
                            .map(item -> new StockData(item.getName(), shop.getPrice(item), shop.getItemCount(item)))
                            .collect(Collectors.toList())))
                .collect(Collectors.toList());

        // workshops
        List<WorkshopData> workshops = world.getWorkshops().stream()
                .map(ws -> new WorkshopData(ws.getName(), ws.getLocation().getLat(), ws.getLocation().getLon()))
                .collect(Collectors.toList());

        // charging stations
        List<ChargingStationData> stations = world.getChargingStations().stream()
                .map(cs -> new ChargingStationData(cs.getName(), cs.getLocation().getLat(),
                                                   cs.getLocation().getLon(), cs.getRate()))
                .collect(Collectors.toList());

        // dumps
        List<DumpData> dumps = world.getDumps().stream().map(dump -> new DumpData(
                dump.getName(), dump.getLocation().getLat(), dump.getLocation().getLon())).collect(Collectors.toList());

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

        //create job data
        List<JobData> commonJobs = world.getJobs().stream()
                // add active regular jobs and auction jobs in auctioning phase
                .filter(job -> ( (!(job instanceof AuctionJob)) && job.isActive() )
                            || ( job instanceof AuctionJob && job.getStatus() == Job.JobStatus.AUCTION ))
                .map(job -> job.toJobData(false))
                .collect(Collectors.toList());

        Map<String, List<JobData>> jobsPerTeam = new HashMap<>();
        world.getTeams().forEach(team -> {
            List<JobData> teamJobs = new Vector<>(commonJobs);
            // add auctions only visible to the assigned team
            world.getJobs().stream()
                    .filter(job -> job instanceof AuctionJob
                            && ((AuctionJob)job).getAuctionWinner().equals(team.getName())
                            && job.isActive())
                    .forEach(job -> teamJobs.add(job.toJobData(true)));
        });

        // create snapshot of the world
        lastSnapShot = new DynamicCityData(
                stepNo,
                new ArrayList<>(completeEntities.values()),
                shops,
                workshops,
                stations,
                dumps,
                world.getJobs().stream()
                        .map(job -> job.toJobData(true))
                        .collect(Collectors.toList()),
                world.getStorages().stream()
                        .map(s -> s.toStorageData(world.getTeams().stream()
                                .map(TeamState::getName)
                                .collect(Collectors.toList())))
                        .collect(Collectors.toList()));

        // create and deliver percepts
        Map<String, RequestAction> percepts = new HashMap<>();
        world.getAgents().forEach(agent -> {
            String team = world.getTeamForAgent(agent);
            percepts.put(agent,
                    new CityStepPercept(
                            completeEntities.get(agent),
                            team, stepNo, teamData.get(team), entities, shops, workshops, stations, dumps,
                            storageMap.get(team), jobsPerTeam
            ));
        });
        return percepts;
    }

    @Override
    public void step(int stepNo, Map<String, Action> actions) {
        // execute all actions in random order
        List<String> agents = world.getAgents();
        RNG.shuffle(agents);
        actionExecutor.preProcess();
        for(String agent: agents)
            actionExecutor.execute(agent, actions);
        actionExecutor.postProcess();
        world.getShops().forEach(Shop::step);

        // process new jobs (created in this step)
        world.processNewJobs();

        // tell all jobs which have to end that they have to end
        world.getJobs().stream().filter(job -> job.getEndStep() == stepNo).forEach(Job::terminate);

        // assign auction jobs which have finished auctioning
        world.getJobs().stream()
                .filter(job -> job instanceof AuctionJob && job.getBeginStep() + ((AuctionJob)job).getAuctionTime() - 1 == stepNo)
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
        return lastSnapShot;
    }

    @Override
    public StaticWorldData getStaticData() {
        return staticData;
    }

    /**
     * Gives items to an entity/agent, if both exist and capacity allows.
     * @param itemName the item type to give
     * @param agentName the name of the agent to receive the item
     * @param amount how many items to give
     * @return true if giving was successful
     */
    public boolean simGive(String itemName, String agentName, int amount){
        Item item = world.getItem(itemName);
        if(item == null) return false;
        Entity entity = world.getEntity(agentName);
        if(entity != null) return entity.addItem(item, amount);
        return false;
    }

    /**
     * Stores items in a storage if both exist.
     * @param storageName name of a storage
     * @param itemName name of an item
     * @param team name of a team
     * @param amount how many items to store
     * @return whether storing was successful
     */
    public boolean simStore(String storageName, String itemName, String team, int amount){
        Optional<Storage> storage = world.getStorages().stream().filter(s -> s.getName().equals(storageName)).findAny();
        if(storage.isPresent()){
            Item item = world.getItem(itemName);
            if(item != null) return storage.get().store(item, amount, team);
        }
        return false;
    }

    /**
     * Adds a job to the simulation. All required items and the storage must exist. Otherwise, the job is not added.
     * @param requirements the items that need to be delivered to the job
     * @param reward the reward for completing the job
     * @param storageName name of the associated storage
     * @param start when the job should start. if the step has already passed, the job will not be activated at all
     * @param end the job's deadline
     */
    public void simAddJob(Map<String, Integer> requirements, int reward, String storageName, int start, int end){
        Optional<Storage> storage = world.getStorages().stream().filter(s -> s.getName().equals(storageName)).findAny();
        if(!storage.isPresent()) return;
        Job job = new Job(reward, storage.get(), start, end);
        requirements.forEach((itemName, amount) -> {
            Item item = world.getItem(itemName);
            if(item == null) return;
            job.addRequiredItem(item, amount);
        });
        world.addJob(job);
    }
}
