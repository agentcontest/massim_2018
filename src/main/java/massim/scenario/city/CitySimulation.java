package massim.scenario.city;

import massim.Action;
import massim.Log;
import massim.RNG;
import massim.config.TeamConfig;
import massim.messages.SimEndContent;
import massim.messages.SimStartPercept;
import massim.messages.StepPercept;
import massim.scenario.AbstractSimulation;
import massim.scenario.city.data.Entity;
import massim.scenario.city.data.Item;
import massim.scenario.city.data.Job;
import massim.scenario.city.data.WorldState;
import massim.scenario.city.data.facilities.Shop;
import massim.scenario.city.data.facilities.Storage;
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
    public Map<String, SimStartPercept> init(int steps, JSONObject config, Set<TeamConfig> matchTeams) {

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
        Map<String, SimStartPercept> initialPercepts = new HashMap<>();
        world.getAgents().forEach(agName -> initialPercepts.put(agName, new CityInitialPercept(agName, world)));
        return initialPercepts;
    }

    @Override
    public Map<String, StepPercept> preStep(int stepNo) {

        Map<String, StepPercept> percepts = new HashMap<>();

        // create team data
        Map<String, CityStepPercept.TeamData> teamData = new HashMap<>();
        world.getTeams().forEach(team -> teamData.put(team.getName(), new CityStepPercept.TeamData(team.getMoney())));

        // create entity data
        List<CityStepPercept.EntityData> entities = new Vector<>();
        world.getEntities().forEach(entity -> entities.add(new CityStepPercept.EntityData(world, entity, false)));

        //create facility data
        List<CityStepPercept.ShopData> shops = new Vector<>();
        world.getShops().forEach(shop -> shops.add(new CityStepPercept.ShopData(shop)));
        List<CityStepPercept.WorkshopData> workshops = new Vector<>();
        world.getWorkshops().forEach(ws -> workshops.add(new CityStepPercept.WorkshopData(ws.getName(),
                ws.getLocation().getLat(), ws.getLocation().getLon())));
        List<CityStepPercept.ChargingStationData> stations = new Vector<>();
        world.getChargingStations().forEach(cs -> stations.add(new CityStepPercept.ChargingStationData(
                cs.getName(), cs.getLocation().getLat(), cs.getLocation().getLon(), cs.getRate())));
        List<CityStepPercept.DumpData> dumps = new Vector<>();
        world.getDumps().forEach(dump -> dumps.add(new CityStepPercept.DumpData(
                dump.getName(), dump.getLocation().getLat(), dump.getLocation().getLon())));
        Map<String, List<CityStepPercept.StorageData>> storageMap = new HashMap<>();
        world.getTeams().forEach(team -> {
            List<CityStepPercept.StorageData> storage = new Vector<>();
            world.getStorages().forEach(st -> storage.add(new CityStepPercept.StorageData(st, team.getName(), world)));
            storageMap.put(team.getName(), storage);
        });

        //create job data
        List<CityStepPercept.JobData> jobs = new ArrayList<>();
        world.getJobs().stream().filter(Job::isActive).forEach(job -> jobs.add(new CityStepPercept.JobData(job)));

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
        // step job generator
        generator.generateJobs(stepNo, world).forEach(job -> world.addJob(job));
        // execute all actions in random order
        List<String> agents = world.getAgents();
        RNG.shuffle(agents);
        actionExecutor.preProcess();
        for(String agent: agents){
            actionExecutor.execute(agent, actions);
        }
        actionExecutor.postProcess();
        world.processNewJobs();
        world.getShops().forEach(Shop::step);
    }

    @Override
    public Map<String, SimEndContent> finish() {
        // TODO calculate rankings!!
        Map<String, SimEndContent> results = new HashMap<>();
        world.getAgents().forEach(agent -> {
            results.put(agent, new SimEndContent(0, world.getTeam(world.getTeamForAgent(agent)).getMoney()));
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

    public void simAddJob(){
        Job job = new Job(1001, Job.SOURCE_SYSTEM, world.getStorages().iterator().next(), 1, 11);
        world.getItems().forEach(item -> job.addRequiredItem(item, 7));
        job.activate();
        world.addJob(job);
        world.processNewJobs();
    }
}
