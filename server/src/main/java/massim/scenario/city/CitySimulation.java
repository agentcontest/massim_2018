package massim.scenario.city;

import massim.messages.Action;
import massim.Log;
import massim.RNG;
import massim.config.TeamConfig;
import massim.messages.SimEndContent;
import massim.messages.SimStartContent;
import massim.messages.RequestActionContent;
import massim.scenario.AbstractSimulation;
import massim.scenario.city.data.*;
import massim.scenario.city.data.facilities.Shop;
import massim.scenario.city.data.facilities.Storage;
import massim.scenario.city.data.jaxb.*;
import massim.scenario.city.percept.CityInitialPercept;
import massim.scenario.city.percept.CityStepPercept;
import massim.scenario.city.util.Generator;
import org.json.JSONObject;

import java.util.*;

/**
 * Main class of the City scenario (2017).
 * @author ta10
 */
public class CitySimulation extends AbstractSimulation {

    private WorldState world;
    private ActionExecutor actionExecutor;
    private Generator generator;

    @Override
    public Map<String, SimStartContent> init(int steps, JSONObject config, Set<TeamConfig> matchTeams) {

        // build the random generator
        JSONObject randomConf = config.optJSONObject("generate");
        if(randomConf == null){
            Log.log(Log.ERROR, "No random generation parameters!");
            randomConf = new JSONObject();
        }
        generator = new Generator(randomConf);

        // create the most important things
        world = new WorldState(steps, config, matchTeams, generator);
        actionExecutor = new ActionExecutor(world);

        // determine initial percepts
        Map<String, SimStartContent> initialPercepts = new HashMap<>();
        world.getAgents().forEach(agName -> initialPercepts.put(agName, new CityInitialPercept(agName, world)));
        return initialPercepts;
    }

    @Override
    public Map<String, RequestActionContent> preStep(int stepNo) {

        // step job generator
        generator.generateJobs(stepNo, world).forEach(job -> world.addJob(job));

        // activate jobs for this step
        world.getJobs().stream().filter(job -> job.getBeginStep() == stepNo).forEach(Job::activate);

        // create and send percepts
        Map<String, RequestActionContent> percepts = new HashMap<>();

        // create team data
        Map<String, TeamData> teamData = new HashMap<>();
        world.getTeams().forEach(team -> teamData.put(team.getName(), new TeamData(team.getMoney())));

        // create entity data
        List<EntityData> entities = new Vector<>();
        world.getEntities().forEach(entity -> entities.add(new EntityData(world, entity, false)));

        //create facility data
        List<ShopData> shops = new Vector<>();
        world.getShops().forEach(shop -> shops.add(new ShopData(shop)));
        List<WorkshopData> workshops = new Vector<>();
        world.getWorkshops().forEach(ws -> workshops.add(new WorkshopData(ws.getName(),
                ws.getLocation().getLat(), ws.getLocation().getLon())));
        List<ChargingStationData> stations = new Vector<>();
        world.getChargingStations().forEach(cs -> stations.add(new ChargingStationData(
                cs.getName(), cs.getLocation().getLat(), cs.getLocation().getLon(), cs.getRate())));
        List<DumpData> dumps = new Vector<>();
        world.getDumps().forEach(dump -> dumps.add(new DumpData(
                dump.getName(), dump.getLocation().getLat(), dump.getLocation().getLon())));
        Map<String, List<StorageData>> storageMap = new HashMap<>();
        world.getTeams().forEach(team -> {
            List<StorageData> storage = new Vector<>();
            world.getStorages().forEach(st -> storage.add(new StorageData(st, team.getName(), world.getItems())));
            storageMap.put(team.getName(), storage);
        });

        //create job data
        List<JobData> jobs = new ArrayList<>();
        world.getJobs().stream().filter(Job::isActive).forEach(job -> jobs.add(new JobData(job)));

        //create and deliver percepts
        world.getAgents().forEach(agent -> {
            String team = world.getTeamForAgent(agent);
            percepts.put(agent,
                    new CityStepPercept(agent, world, stepNo, teamData.get(team), entities,
                            shops, workshops, stations, dumps, storageMap.get(team), jobs));
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
        world.processNewJobs();
        world.getShops().forEach(Shop::step);

        // tell all jobs which have to end that they have to end
        world.getJobs().stream().filter(job -> job.getEndStep() == stepNo).forEach(Job::terminate);

        // assign auction jobs which have finished auctioning
        world.getJobs().stream()
                .filter(job -> job instanceof AuctionJob && job.getBeginStep() + ((AuctionJob)job).getAuctionTime() - 1 == stepNo)
                .forEach(job -> ((AuctionJob)job).assign());
    }

    @Override
    public Map<String, SimEndContent> finish() {

        // calculate ranking
        Map<TeamState, Integer> rankings = new HashMap<>();
        Map<Long, Set<TeamState>> scoreToTeam = new HashMap<>();
        world.getTeams().forEach(team -> {
            scoreToTeam.putIfAbsent(team.getMoney(), new HashSet<>());
            scoreToTeam.get(team.getMoney()).add(team);
        });
        List<Long> scoreRanking = new ArrayList<>(scoreToTeam.keySet());
        Collections.sort(scoreRanking); // sort ascending
        Collections.reverse(scoreRanking); // now descending
        final int[] ranking = {1};
        scoreRanking.forEach(score -> {
            Set<TeamState> teams = scoreToTeam.get(score);
            teams.forEach(team -> rankings.put(team, ranking[0]));
            ranking[0] += teams.size();
        });

        // create percepts
        Map<String, SimEndContent> results = new HashMap<>();
        world.getAgents().forEach(agent -> {
            TeamState team = world.getTeam(world.getTeamForAgent(agent));
            results.put(agent, new SimEndContent(rankings.get(team), team.getMoney()));
        });
        return results;
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
