package massim.scenario.city;

import massim.config.TeamConfig;
import massim.protocol.Message;
import massim.protocol.messagecontent.Action;
import massim.protocol.messagecontent.RequestAction;
import massim.protocol.messagecontent.SimStart;
import massim.protocol.scenario.city.data.ActionData;
import massim.protocol.scenario.city.data.JobData;
import massim.protocol.scenario.city.percept.CityInitialPercept;
import massim.protocol.scenario.city.percept.CityStepPercept;
import massim.scenario.city.data.*;
import massim.scenario.city.data.facilities.*;
import massim.util.Conversions;
import massim.util.IOUtil;
import massim.util.Log;
import massim.util.RNG;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * (Integration) Testing (important aspects of) the City scenario.
 */
public class CitySimulationTest {

    /**
     * The shared simulation object (since creating a new one for each test is kind of expensive)
     */
    private static CitySimulation sim;

    private static int seed = 17;
    private static int agentsPerTeam = 30;
    private static int steps = 10000;
    private static int step = 0;

    /**
     * Sets up a blank initialized simulation that can be used for all tests.
     */
    @BeforeClass
    public static void setup() throws IOException {
        RNG.initialize(seed);

        sim = new CitySimulation();

        // create config
        JSONObject matchConf = IOUtil.readJSONObject("conf/QuickTest.json").getJSONArray("match").getJSONObject(0);

        // setup teams
        Set<TeamConfig> teams = new HashSet<>(Arrays.asList(new TeamConfig("A"), new TeamConfig("B")));
        for(int i = 1; i <= agentsPerTeam; i++){
            for (TeamConfig team : teams) {
                team.addAgent("agent" + team.getName() + i, "1");
            }
        }

        Map<String, SimStart> initialPercepts = sim.init(steps, matchConf, teams);
        SimStart percept = initialPercepts.get("agentA1");
//        Log.log(Log.Level.NORMAL, Conversions.docToString(new Message(System.currentTimeMillis(), percept).toXML(), true));
        assert percept instanceof CityInitialPercept;
        CityInitialPercept initialPercept = (CityInitialPercept) percept;
        assert initialPercept.getItemData().size() > 0;
        assert initialPercept.getMapName() != null;
        assert !initialPercept.getMapName().equals("");
        assert initialPercept.getRoleData() != null;
    }

    /**
     * Logs info before each test
     */
    @Before
    public void logInfo(){
        Log.log(Log.Level.NORMAL, "Current step: " + step);
    }

    /**
     * Make sure each test method uses a new step number.
     */
    @After
    public void incrementStep(){
        step++;
    }

    @Test
    public void actionIsPerceived() {

        Map<String, Action> actions = buildActionMap();
        actions.put("agentA1", new Action("give", "agentA2", "item0", "1"));
        sim.preStep(step);
        sim.step(step++, actions);

        Map<String, RequestAction> percepts = sim.preStep(step);
        ActionData action = getPercept("agentA1", percepts).getSelfData().getLastAction();
        assert(action.getType().equals("give"));
        assert(action.getParams().get(0).equals("agentA2"));
        assert(action.getParams().get(1).equals("item0"));
        assert(action.getParams().get(2).equals("1"));
        assert(action.getResult().equals("failed_counterpart"));
        sim.step(step, buildActionMap());
    }

    /**
     * Checks whether most things are included in the percept as expected.
     */
    @Test
    public void perceptIsComplete(){

        // add some perceivable jobs
        Storage storage = (Storage) sim.getWorldState().getFacility("storage1");
        TeamState teamA = sim.getWorldState().getTeam("A");
        Item item = sim.getWorldState().getItemByName("item0");

        ItemBox items = new ItemBox();
        items.store(item, 3);
        Mission mission = new Mission(1000, storage, step + 1, step + 100, 1000, items, teamA, "myMission");
        sim.getWorldState().addJob(mission);
        AuctionJob auction = new AuctionJob(1001, storage, step + 1, step + 100, items, 2, 10002);
        sim.getWorldState().addJob(auction);
        Job job = new Job(777, storage, step + 1, step + 100, items, JobData.POSTER_SYSTEM);
        sim.getWorldState().addJob(job);

        // store something
        storage.store(item, 2, "A");

        // move agent to resource node
        Entity e1 = sim.getWorldState().getEntity("agentA1");
        ResourceNode node = (ResourceNode) sim.getWorldState().getFacility("node1");
        e1.setLocation(node.getLocation());
        //move another entity in perception range
        Entity e2 = sim.getWorldState().getEntity("agentA2");
        e2.setLocation(e1.getLocation());
        // give an item to the agent
        e1.addItem(item, 1);
        e1.addItem(sim.getWorldState().getItemByName("item1"), 1);

        // one step for activating jobs
        sim.preStep(step);
        Map<String, Action> actions = buildActionMap();
        sim.step(step, actions);
        step++;
        sim.preStep(step);

        // let the agent execute an action
        actions = buildActionMap();
        actions.put("agentA1", new Action("goto", "shop1"));
        sim.step(step, actions);
        step++;

        // one step for getting the final percept(s)
        Map<String, RequestAction> percepts = sim.preStep(step);
        CityStepPercept percept = (CityStepPercept) percepts.get("agentA1");

        // uncomment to print example request-action message
        Log.log(Log.Level.NORMAL, Conversions.docToString(new Message(System.currentTimeMillis(), percept).toXML(), true));

        // check if percept contains the important things
        assert percept.getShopData().size() > 0;
        assert percept.getWorkshops().size() > 0;
        assert percept.getDumps().size() > 0;
        assert percept.getChargingStations().size() > 0;
        assert percept.getStorage().size() > 0;
        assert percept.getAuctions().size() > 0;
        assert percept.getJobs().size() > 0;
        assert percept.getMissions().size() > 0;
        assert percept.getEntityData().size() > 0;
        assert percept.getSelfData() != null;
        assert percept.getSimData() != null;
        assert percept.getSelfData().getCharge() == e1.getCurrentBattery();
        assert percept.getSelfData().getItems().size() > 0;

        sim.step(step, buildActionMap());
    }

    @Test
    public void gotoWorks(){

        // determine a shop as goto target
        Shop shop = sim.getWorldState().getShops().iterator().next();

        // let all agents go somewhere
        sim.preStep(step);

        Map<String, Action> actions = buildActionMap();
        actions.put("agentA1", new Action("goto",
                String.valueOf(shop.getLocation().getLat()), String.valueOf(shop.getLocation().getLon())));
        actions.put("agentA2", new Action("goto", "resourceNode1"));
        actions.put("agentA3", new Action("goto", shop.getName()));

        sim.step(step, actions);

        // check results and new locations
        assert(sim.getWorldState().getEntity("agentA1").getLocation().equals(shop.getLocation()));
        assert(sim.getWorldState().getEntity("agentA2").getLastActionResult().equals("failed_unknown_facility"));
        assert(sim.getWorldState().getEntity("agentA3").getLocation().equals(shop.getLocation()));
    }

    @Test
    public void giveReceiveWorks(){

        // move one agent to another and give her some item
        Entity e4  = sim.getWorldState().getEntity("agentA4");
        Entity e5  = sim.getWorldState().getEntity("agentA5");
        Item item = sim.getWorldState().getItems().get(0);

        e4.setLocation(e5.getLocation());
        e4.clearInventory();
        e5.clearInventory();
        e4.addItem(item, 1);

        // assert preconditions
        assert(e4.getItemCount(item) == 1);
        assert(e5.getItemCount(item) == 0);

        // give and receive some items
        sim.preStep(step);
        Map<String, Action> actions = buildActionMap();
        actions.put("agentA4", new Action("give", "agentA5", item.getName(), "1"));
        actions.put("agentA5", new Action("receive"));
        sim.step(step, actions);

        assert(e4.getItemCount(item) == 0);
        assert(e5.getItemCount(item) == 1);
    }

    @Test
    public void storeRetrieveWorks(){
        WorldState world = sim.getWorldState();
        Entity e2 = world.getEntity("agentA2");
        Item item = world.getItems().get(0);
        Storage storage = world.getStorages().iterator().next();

        e2.setLocation(storage.getLocation());
        e2.clearInventory();
        e2.addItem(item, 2);
        storage.removeStored(item, 100, "A");

        // preconditions
        assert(storage.getStored(item, "A") == 0);
        assert(e2.getItemCount(item) == 2);

        // store something
        sim.preStep(step);
        Map<String, Action> actions = buildActionMap();
        actions.put("agentA2", new Action("store", item.getName(), "1"));
        sim.step(step, actions);

        assert(storage.getStored(item, "A") == 1);
        assert(e2.getItemCount(item) == 1);

        step++;

        // retrieve it
        sim.preStep(step);
        actions = buildActionMap();
        actions.put("agentA2", new Action("retrieve", item.getName(), "1"));
        sim.step(step, actions);

        assert(storage.getStored(item, "A") == 0);
        assert(e2.getItemCount(item) == 2);

        step++;

        // retrieve too much
        sim.preStep(step);
        sim.step(step, actions); // actions can be reused

        assert(storage.getStored(item, "A") == 0);
        assert(e2.getItemCount(item) == 2);
        assert(e2.getLastActionResult().equals("failed_item_amount"));

        step++;

        // store too much
        int fill = storage.getCapacity() / item.getVolume();
        storage.store(item, fill, "A");
        e2.addItem(item, 1);
        int carrying = e2.getItemCount(item);

        sim.preStep(step);
        actions = buildActionMap();
        actions.put("agentA2", new Action("store", item.getName(), "1"));
        sim.step(step, actions);

        assert(storage.getStored(item, "A") == fill);
        assert(e2.getItemCount(item) == carrying);
        assert(e2.getLastActionResult().equals("failed_capacity"));
    }

    @Test
    public void assembleWorks(){

        WorldState world = sim.getWorldState();
        Entity e1 = world.getEntity("agentA1");
        Entity e2 = world.getEntity("agentA2");
        Entity e3 = world.getEntity("agentA3");
        Entity e4 = world.getEntity("agentA20");
        Workshop workshop = world.getWorkshops().iterator().next();
        Optional<Item> optItem = world.getItems().stream() // find item that needs roles and materials
                .filter(item -> item.getRequiredItems().size() > 1 && item.getRequiredRoles().size() > 0)
                .findAny();
        assert optItem.isPresent();
        Item item = optItem.get();

        e1.clearInventory();
        e1.setLocation(workshop.getLocation());
        Map<String, Action> actions = buildActionMap();
        actions.put("agentA1", new Action("assemble", item.getName()));
        for (int i = 2; i < 30; i++){
            String agName = "agentA" + i;
            Entity ent = world.getEntity(agName);
            ent.clearInventory();
            ent.setLocation(workshop.getLocation());
            actions.put(agName, new Action("assist_assemble", "agentA1"));
        }

        Entity[] assistants = new Entity[]{e2, e3, e4};
        List<Item> requiredItems = new ArrayList<>(item.getRequiredItems());
        e1.addItem(requiredItems.get(0), 1);
        for(int i = 1; i < requiredItems.size(); i++)
            assistants[i%assistants.length].addItem(requiredItems.get(i), 1);

        // check assembly

        sim.preStep(step);
        sim.step(step, actions);

        assert e1.getLastActionResult().equals("successful");
        assert e2.getLastActionResult().equals("successful");
        assert e1.getItemCount(item) == 1;
        item.getRequiredItems().forEach(req -> {
            assert e1.getItemCount(req) == 0;
            assert e2.getItemCount(req) == 0;
        });
    }

    @Test
    public void dumpWorks(){
        WorldState world = sim.getWorldState();
        Entity e1 = world.getEntity("agentA1");
        Dump dump = world.getDumps().iterator().next();
        Item item = world.getItems().get(0);

        e1.clearInventory();
        e1.addItem(item, 7);
        e1.setLocation(dump.getLocation());

        sim.preStep(step);
        Map<String, Action> actions = buildActionMap();
        actions.put("agentA1", new Action("dump", item.getName(), "4"));
        sim.step(step, actions);

        assert(e1.getItemCount(item) == 3);
    }

    @Test
    public void chargeWorks(){
        WorldState world = sim.getWorldState();
        Entity e1 = world.getEntity("agentA1");
        ChargingStation station = world.getChargingStations().iterator().next();

        e1.discharge();
        e1.setLocation(station.getLocation());

        assert e1.getCurrentBattery() == 0;

        sim.preStep(step);
        Map<String, Action> actions = buildActionMap();
        actions.put("agentA1", new Action("charge"));
        sim.step(step, actions);

        assert e1.getCurrentBattery() == Math.min(e1.getRole().getMaxBattery(), station.getRate());
    }

    @Test
    public void rechargeWorks(){
        WorldState world = sim.getWorldState();
        Entity e2 = world.getEntity("agentA2");
        Shop shop = world.getShops().iterator().next();

        e2.setLocation(shop.getLocation()); // make sure the agent is not in a charging station
        e2.discharge();

        assert e2.getCurrentBattery() == 0;

        sim.preStep(step);
        Map<String, Action> actions = buildActionMap();
        actions.put("agentA2", new Action("recharge"));
        sim.step(step, actions);

        assert e2.getLastActionResult().equalsIgnoreCase("failed") || e2.getCurrentBattery() == 1;
    }

    @Test
    public void gatherWorks(){
        WorldState world = sim.getWorldState();
        Entity e1 = world.getEntity("agentA1");
        ResourceNode node = world.getResourceNodes().iterator().next();
        Item item = node.getResource();

        e1.clearInventory();
        e1.setLocation(node.getLocation());

        assert e1.getItemCount(item) == 0;

        // check if the agent gathers at least once in 10 steps

        Map<String, Action> actions = buildActionMap();
        actions.put("agentA1", new Action("gather"));
        for(int i = 0; i < 10; i++){
            sim.preStep(step);
            sim.step(step, actions);
            step++;
        }

        assert e1.getItemCount(item) > 0;
    }

    @Test
    public void jobActionsWork(){
        Map<String, Action> actions = buildActionMap();
        WorldState world = sim.getWorldState();
        Storage storage = world.getStorages().iterator().next();
        Entity eA = world.getEntity("agentA1");
        Entity eB = world.getEntity("agentB1");
        Item item = world.getItems().get(0);
        long moneyA = world.getTeam("A").getMassium();
        int reward = 77777;
        ItemBox requirements = new ItemBox();
        requirements.store(item, 5);

        eA.clearInventory();
        eB.clearInventory();
        eA.setLocation(storage.getLocation());
        eB.setLocation(storage.getLocation());
        eA.addItem(item, 3);
        eB.addItem(item, 1);
        storage.removeDelivered(item, 10000, "A");
        storage.removeDelivered(item, 10000, "B");
        Job job = new Job(reward, storage, step + 1, step + 4, requirements, JobData.POSTER_SYSTEM);
        world.addJob(job);

        // activate job
        sim.preStep(step);
        sim.step(step++, actions);

        // check delivering (partial)
        actions = buildActionMap();
        actions.put("agentA1", new Action("deliver_job", job.getName()));
        actions.put("agentB1", new Action("deliver_job", job.getName()));

        sim.preStep(step);
        sim.step(step++, actions);

        assert eA.getItemCount(item) == 0;
        assert eB.getItemCount(item) == 0;
        assert eA.getLastActionResult().equals("successful_partial");
        assert eB.getLastActionResult().equals("successful_partial");

        // check completion
        eA.addItem(item, 3);

        sim.preStep(step);
        sim.step(step, actions);

        assert eA.getItemCount(item) == 1;
        assert eA.getLastActionResult().equals("successful");
        assert world.getTeam("A").getMassium() == moneyA + reward;
        assert storage.getDelivered(item, "A") == 0;
        assert storage.getDelivered(item, "B") == 1;

        step++;

        // retrieve delivery

        actions = buildActionMap();
        actions.put("agentB1", new Action("retrieve_delivered", item.getName(), "1"));

        sim.preStep(step);
        sim.step(step, actions);

        assert eB.getItemCount(item) == 1;
    }

    @Test
    public void bidWorks(){
        WorldState world = sim.getWorldState();
        Storage storage = world.getStorages().iterator().next();
        Item item = world.getItems().get(0);
        ItemBox itemsRequired = new ItemBox();
        itemsRequired.store(item, 1);
        AuctionJob auction = new AuctionJob(999, storage, step + 1, step + 4, itemsRequired, 2, 888);
        AuctionJob auction2 = new AuctionJob(999, storage, step + 1, step + 4, itemsRequired, 2, 888);
        world.addJob(auction);
        world.addJob(auction2);
        long moneyA = world.getTeam("A").getMassium();
        long moneyB = world.getTeam("B").getMassium();
        Entity eB = world.getEntity("agentB1");

        Map<String, Action> actions = buildActionMap();
        sim.preStep(step);
        sim.step(step, actions); // let auctions get names and be registered

        step++;

        actions.put("agentA1", new Action("bid_for_job", auction.getName(), "1000"));
        actions.put("agentB1", new Action("bid_for_job", auction2.getName(), "998"));
        sim.preStep(step);
        sim.step(step, actions);

        assert auction.getLowestBid() == null;
        assert auction2.getLowestBid() == 998;

        step++;

        actions = buildActionMap();
        actions.put("agentA1", new Action("bid_for_job", auction.getName(), "778"));
        sim.preStep(step);
        sim.step(step, actions);

        assert auction.getLowestBid() == 778;

        step++;

        // complete auction for team B

        eB.addItem(item, 1);
        eB.setLocation(storage.getLocation());

        actions = buildActionMap();
        actions.put("agentB1", new Action("deliver_job", auction2.getName()));
        sim.preStep(step);
        sim.step(step, actions);

        assert eB.getLastActionResult().equals("successful");

        step++;

        // check if team A paid the fine and B got the reward

        sim.preStep(step);
        sim.step(step, buildActionMap());

        assert world.getTeam("A").getMassium() == moneyA - auction.getFine();
        assert world.getTeam("B").getMassium() == moneyB + auction2.getLowestBid();
    }

    @Test
    public void buildingWorks() {
        WorldState world = sim.getWorldState();
        Entity agentA1 = world.getEntity("agentA1");
        Entity agentB1 = world.getEntity("agentB1");
        double lat = world.getMinLat() + (world.getMaxLat() - world.getMinLat()) / 2;
        double lon = world.getMinLon() + (world.getMaxLon() - world.getMinLon()) / 2;
        Location loc = new Location(lon, lat);
        agentA1.setLocation(loc);
        agentB1.setLocation(loc);
        TeamState team = world.getTeam("A");
        long mass = team.getMassium();
        WellType wellType = world.getWellTypes().iterator().next();
        team.addMassium(wellType.getCost());
        assert team.getScore() == 0;
        assert team.getMassium() >= wellType.getCost();

        Map<String, Action> actions = buildActionMap();
        actions.put("agentA1", new Action("build", wellType.getName()));

        sim.preStep(step);
        sim.step(step++, actions);

        assert agentA1.getLastActionResult().equalsIgnoreCase("successful");
        assert world.getWells().size() == 1;
        assert team.getMassium() == mass;

        Well well = world.getWells().iterator().next();
        int integrity = well.getIntegrity();
        actions.put("agentA1", new Action("build"));

        sim.preStep(step);
        sim.step(step++, actions);

        // check if well was built
        assert well.getIntegrity() > integrity;
        assert team.getScore() > 0;

        actions = buildActionMap();
        actions.put("agentB1", new Action("dismantle"));

        // check if well can be dismantled
        integrity = well.getIntegrity();
        while(true) {
            sim.preStep(step);
            sim.step(step++, actions);
            assert well.getIntegrity() < integrity;
            integrity = well.getIntegrity();
            if (integrity == 0) break;
        }

        assert well.getIntegrity() == 0;

        // check if score remains the same without the well
        long score = team.getScore();
        actions = buildActionMap();
        sim.preStep(step);
        sim.step(step++ ,actions);
        assert team.getScore() == score;
    }

    @Test
    public void upgradesWork() {
        WorldState world = sim.getWorldState();
        Upgrade upgrade = world.getUpgrade("load");
        Entity agentA1 = world.getEntity("agentA1");
        TeamState teamA = world.getTeam("A");
        Shop shop = world.getShops().get(0);
        agentA1.setLocation(shop.getLocation());
        teamA.subMassium(teamA.getMassium());
        teamA.addMassium(upgrade.getCost());
        int load = agentA1.getLoadCapacity();
        Map<String, Action> actions = buildActionMap();
        actions.put("agentA1", new Action("upgrade", "load"));
        // check 1 upgrade
        sim.preStep(step);
        sim.step(step++, actions);
        assert agentA1.getLastActionResult().equalsIgnoreCase("successful");
        assert agentA1.getLoadCapacity() == load + upgrade.getStep();
        assert teamA.getMassium() == 0;
        // check upgrade when fully upgraded
        while(agentA1.getLoadCapacity() < agentA1.getRole().getMaxLoad()) {
            agentA1.upgrade(upgrade);
        }
        teamA.addMassium(upgrade.getCost());
        sim.preStep(step);
        sim.step(step++, actions);
        assert agentA1.getLoadCapacity() == agentA1.getRole().getMaxLoad();
        assert teamA.getMassium() == 0;
    }

    @Test
    public void chargingStationsWork(){
        //there is at least one charging station in the simulation
        assert !sim.getWorldState().getChargingStations().isEmpty();
    }

    @Test
    public void shopsWork(){
        WorldState world = sim.getWorldState();
        Item resource = world.getResources().get(0);
        Item assembledItem = world.getAssembledItems().get(0);
        Shop shop = world.getShops().get(0);
        Entity agentA1 = world.getEntity("agentA1");
        Entity agentB1 = world.getEntity("agentB1");
        agentA1.setLocation(shop.getLocation());
        agentB1.setLocation(shop.getLocation());
        agentA1.clearInventory();
        agentB1.clearInventory();
        agentA1.addItem(resource, 3);
        agentB1.addItem(assembledItem, 3);

        Map<String, Action> actions = buildActionMap();
        actions.put("agentA1", new Action("trade", resource.getName(), "2"));
        actions.put("agentB1", new Action("trade", assembledItem.getName(), "2"));

        sim.preStep(step);
        sim.step(step++, actions);

        assert agentA1.getItemCount(resource) == 3;
        assert agentB1.getItemCount(assembledItem) == 1;
    }

    @Test
    public void dumpsWork(){
        //there is at least one dump in the simulation
        assert !sim.getWorldState().getDumps().isEmpty();
    }

    @Test
    public void workshopsWork(){
        //there is at least one workshop in the simulation
        assert !sim.getWorldState().getWorkshops().isEmpty();
    }

    @Test
    public void storageWork(){
        //there is at least one storage in the simulation
        assert !sim.getWorldState().getStorages().isEmpty();
    }

    @Test
    public void resourceNodesWork(){
        WorldState world = sim.getWorldState();
        ResourceNode node = world.getResourceNodes().get(0);
        Entity agentA1 = world.getEntity("agentA1");
        agentA1.setLocation(node.getLocation());
        agentA1.clearInventory();

        Map<String, Action> actions = buildActionMap();
        actions.put("agentA1", new Action("gather"));

        sim.preStep(step);
        sim.step(step++, actions);

        assert agentA1.getItemCount(node.getResource()) == agentA1.getSkill() / node.getThreshold();
    }

    @Test
    public void jobsWork(){
        WorldState world = sim.getWorldState();
        for(int i = 0; i < 100; i++) {
            world.getGenerator().generateJobs(i, world).forEach(job -> {
                assert !job.getRequiredItems().getStoredTypes().isEmpty();
                assert job.getReward() > 0;
            });
        }
    }

    @Test
    public void stuckAgentsAreRescued(){
        WorldState world = sim.getWorldState();
        Entity e1 = world.getEntity("agentA1");

        e1.setLocation(new Location(2.34953, 48.86091));

        sim.preStep(step);
        Map<String, Action> actions = buildActionMap();
        actions.put("agentA1", new Action("goto", "shop3"));
        sim.step(step, actions);
        assert e1.getLastActionResult().equals(ActionExecutor.FAILED_NO_ROUTE);
        sim.preStep(step);
        actions.put("agentA1", new Action("goto", "shop3"));
        sim.step(step, actions);
        assert !e1.getLastActionResult().equals(ActionExecutor.FAILED_NO_ROUTE);
    }

    /**
     * @return a new action-map where each agent just skips
     */
    private static Map<String, Action> buildActionMap(){
        return sim.getWorldState().getAgents().stream()
                .collect(Collectors.toMap(ag -> ag, ag -> new Action("skip")));
    }

    /**
     * Checks if a request action contains the correct percept and returns it.
     * @param agent name of an agent
     * @param messages all req act messages received in preStep
     * @return the req act message of the agent cast to the correct percept
     */
    private static CityStepPercept getPercept(String agent, Map<String, RequestAction> messages){
        RequestAction reqAct = messages.get(agent);
        assert(reqAct != null && reqAct instanceof CityStepPercept);
        return (CityStepPercept) reqAct;
    }
}