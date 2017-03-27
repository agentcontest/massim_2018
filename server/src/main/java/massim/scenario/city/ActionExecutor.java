package massim.scenario.city;

import massim.protocol.scenario.city.data.JobData;
import massim.util.Log;
import massim.util.RNG;
import massim.protocol.messagecontent.Action;
import massim.scenario.city.data.*;
import massim.scenario.city.data.facilities.*;

import java.util.*;

import static massim.protocol.scenario.city.Actions.*;

/**
 * How else to execute agent actions.
 */
public class ActionExecutor {

    // scenario-specific failure-codes
    public final static String SUCCESSFUL = "successful";
    private final static String FAILED_COUNTERPART = "failed_counterpart";
    private final static String FAILED_LOCATION = "failed_location";
    private final static String FAILED_NO_ROUTE = "failed_no_route";
    private final static String FAILED_UNKNOWN_ITEM = "failed_unknown_item";
    private final static String FAILED_UNKNOWN_AGENT = "failed_unknown_agent";
    private final static String FAILED_ITEM_AMOUNT = "failed_item_amount";
    private final static String FAILED_CAPACITY = "failed_capacity";
    private final static String FAILED_UNKNOWN_FACILITY = "failed_unknown_facility";
    private final static String FAILED_WRONG_FACILITY = "failed_wrong_facility";
    private final static String FAILED_TOOLS = "failed_tools";
    private final static String FAILED_ITEM_TYPE = "failed_item_type";
    private final static String FAILED_UNKNOWN_JOB = "failed_unknown_job";
    private final static String FAILED_JOB_STATUS = "failed_job_status";
    private final static String FAILED_JOB_TYPE = "failed_job_type";
    private final static String PARTIAL_SUCCESS = "successful_partial"; // part of the job was delivered, still active.
    private final static String FAILED_WRONG_PARAM = "failed_wrong_param";
    private final static String FAILED = "failed";
    private final static String USELESS = "useless";

    private WorldState world;

    /**
     * Contains all agents that actually received items this turn.
     */
    private Set<Entity> receivers;

    /**
     * Contains all agents that want to assemble an item this turn.
     */
    private Set<Entity> assemblers;

    /**
     * Keys: assemblers, Values: sets of assistants
     */
    private Map<Entity, Set<Entity>> assistants;

    ActionExecutor(WorldState world) {
        this.world = world;
    }

    /**
     * Prepares everything for the new step.
     * So, should be called before each step.
     */
    void preProcess(){
        receivers = new HashSet<>();
        assemblers = new HashSet<>();
        assistants = new HashMap<>();
    }

    /**
     * Execute an action for a given agent.
     * @param agent the name of the agent
     * @param actions the actions of all agents
     * @param stepNo the current step
     */
    void execute(String agent, Map<String, Action> actions, int stepNo) {

        Entity entity = world.getEntity(agent);

        // determine random fail
        if (RNG.nextInt(100) < world.getRandomFail()){
            entity.setLastAction(Action.STD_RANDOM_FAIL_ACTION);
            entity.setLastActionResult(FAILED);
            return;
        }

        Action action = actions.get(agent);
        if(action == null){
            Log.log(Log.Level.CRITICAL, "No action for agent " + agent + " provided.");
            action = Action.STD_NO_ACTION;
        }
        entity.setLastAction(action);
        List<String> params = action.getParameters();
        switch (action.getActionType()){

            case Action.NO_ACTION:
                entity.setLastActionResult(SUCCESSFUL);
                break;

            case GO_TO:
                if(params.size() == 0){ // no params => follow existing route
                    if(entity.getRoute() == null){
                        entity.setLastActionResult(FAILED_WRONG_PARAM);
                        break;
                    }
                }
                Location destination;
                if(params.size() == 1){ // param must be facility name
                    Facility facility = world.getFacility(params.get(0));
                    if(facility == null || facility instanceof ResourceNode){
                        entity.setLastActionResult(FAILED_UNKNOWN_FACILITY);
                        break;
                    }
                    destination = facility.getLocation();
                }
                else if(params.size() == 2){ // params must be (lat,lon)
                    destination = Location.parse(params.get(0), params.get(1));
                }
                else{ // too many parameters
                    entity.setLastActionResult(FAILED_WRONG_PARAM);
                    break;
                }
                if (destination != null) entity.setRoute(
                        world.getMap().findRoute(entity.getLocation(), destination, entity.getRole().getPermissions()));
                else{
                    entity.setLastActionResult(FAILED_WRONG_PARAM);
                    break;
                }
                entity.setLastActionResult(entity.advanceRoute(world.getGotoCost())? SUCCESSFUL : FAILED_NO_ROUTE);
                break;

            case GIVE: // 3 params (agent, item, amount)
                if(params.size() != 3){
                    entity.setLastActionResult(FAILED_WRONG_PARAM);
                }
                else {
                    String receiver = params.get(0);
                    Item item = world.getItemOrTool(params.get(1));
                    Entity receiverEntity = world.getEntity(receiver);
                    int amount = -1;
                    try { amount = Integer.parseInt(params.get(2)); } catch (NumberFormatException ignored) {}

                    if(receiverEntity == null || amount < 0){
                        entity.setLastActionResult(FAILED_WRONG_PARAM);
                    }
                    else if (item == null) {
                        entity.setLastActionResult(FAILED_UNKNOWN_ITEM);
                    }
                    else if (!actions.get(receiver).getActionType().equals(RECEIVE)) {
                        entity.setLastActionResult(FAILED_COUNTERPART);
                    }
                    else if (!receiverEntity.getLocation().inRange(entity.getLocation())) {
                        entity.setLastActionResult(FAILED_LOCATION);
                    }
                    else if (amount > entity.getItemCount(item)) {
                        entity.setLastActionResult(FAILED_ITEM_AMOUNT);
                    }
                    else if (receiverEntity.getFreeSpace() < amount * item.getVolume()) {
                        entity.setLastActionResult(FAILED_CAPACITY);
                    }
                    else {
                        entity.transferItems(receiverEntity, item, amount);
                        entity.setLastActionResult(SUCCESSFUL);
                        receivers.add(receiverEntity);
                    }
                }
                break;

            case RECEIVE:
                break; // action is processed in give-action, result in postProcess()

            case STORE: // 2 params (item, amount)
                if(params.size() != 2){
                    entity.setLastActionResult(FAILED_WRONG_PARAM);
                    return;
                }
                Facility facility = world.getFacilityByLocation(entity.getLocation());
                if(facility == null){
                    entity.setLastActionResult(FAILED_LOCATION);
                    return;
                }
                else if(!(facility instanceof Storage)){
                    entity.setLastActionResult(FAILED_WRONG_FACILITY);
                    return;
                }
                Storage storage = (Storage)facility;
                Item item = world.getItemOrTool(params.get(0));
                if(item == null){
                    entity.setLastActionResult(FAILED_UNKNOWN_ITEM);
                    return;
                }
                int amount = -1;
                try{
                    amount = Integer.parseInt(params.get(1));
                } catch(NumberFormatException ignored){}
                if(amount < 1 || amount > entity.getItemCount(item)){
                    entity.setLastActionResult(FAILED_ITEM_AMOUNT);
                    return;
                }
                if(storage.getFreeSpace() < amount * item.getVolume()){
                    entity.setLastActionResult(FAILED_CAPACITY);
                    return;
                }
                if(storage.store(item, amount, world.getTeamForAgent(agent))){
                    entity.setLastActionResult(SUCCESSFUL);
                    entity.removeItem(item, amount);
                }
                else{
                    entity.setLastActionResult(FAILED);
                }
                break;

            case RETRIEVE:           // 2 params (item, amount)
            case RETRIEVE_DELIVERED: // 2 params (item, amount)
                if(params.size() != 2){
                    entity.setLastActionResult(FAILED_WRONG_PARAM);
                    return;
                }
                facility = world.getFacilityByLocation(entity.getLocation());
                if(facility == null){
                    entity.setLastActionResult(FAILED_LOCATION);
                    return;
                }
                else if(!(facility instanceof Storage)){
                    entity.setLastActionResult(FAILED_WRONG_FACILITY);
                    return;
                }
                storage = (Storage)facility;
                item = world.getItemOrTool(params.get(0));
                if(item == null){
                    entity.setLastActionResult(FAILED_UNKNOWN_ITEM);
                    return;
                }
                amount = -1;
                try{
                    amount = Integer.parseInt(params.get(1));
                } catch(NumberFormatException ignored){}
                int retrievable = action.getActionType().equals(RETRIEVE)?
                                              storage.getStored(item, world.getTeamForAgent(agent))
                                            : storage.getDelivered(item, world.getTeamForAgent(agent));
                if (amount < 1 || amount > retrievable){
                    entity.setLastActionResult(FAILED_ITEM_AMOUNT);
                    return;
                }
                if(amount * item.getVolume() > entity.getFreeSpace()){
                    entity.setLastActionResult(FAILED_CAPACITY);
                    return;
                }
                if(action.getActionType().equals(RETRIEVE))
                    storage.removeStored(item, amount, world.getTeamForAgent(agent));
                else
                    storage.removeDelivered(item, amount, world.getTeamForAgent(agent));
                entity.addItem(item, amount);
                entity.setLastActionResult(SUCCESSFUL);
                break;

            case ASSEMBLE: // 1 param (item)
                if(params.size() != 1){
                    entity.setLastActionResult(FAILED_WRONG_PARAM);
                    break;
                }
                facility = world.getFacilityByLocation(entity.getLocation());
                if(facility == null){
                    entity.setLastActionResult(FAILED_LOCATION);
                    break;
                }
                else if(!(facility instanceof Workshop)){
                    entity.setLastActionResult(FAILED_WRONG_FACILITY);
                    break;
                }
                assemblers.add(entity);
                assistants.putIfAbsent(entity, new HashSet<>());
                break;

            case ASSIST_ASSEMBLE: // 1 param (agent)
                if(params.size() != 1){
                    entity.setLastActionResult(FAILED_WRONG_PARAM);
                    break;
                }
                Entity assembler = world.getEntity(params.get(0));
                if (assembler == null){
                    entity.setLastActionResult(FAILED_UNKNOWN_AGENT);
                    break;
                }
                Action counterPartAction = actions.get(params.get(0));
                if(counterPartAction != null && !counterPartAction.getActionType().equals(ASSEMBLE)){
                    entity.setLastActionResult(FAILED_COUNTERPART);
                    break;
                }
                else if(!entity.getLocation().inRange(assembler.getLocation())){
                    entity.setLastActionResult(FAILED_LOCATION);
                    break;
                }
                assistants.putIfAbsent(assembler, new HashSet<>());
                assistants.get(assembler).add(entity);
                break;

            case BUY: // 2 params (item, amount)
                if(params.size() != 2){
                    entity.setLastActionResult(FAILED_WRONG_PARAM);
                    break;
                }
                facility = world.getFacilityByLocation(entity.getLocation());
                if(facility == null){
                    entity.setLastActionResult(FAILED_LOCATION);
                    break;
                }
                else if(!(facility instanceof Shop)){
                    entity.setLastActionResult(FAILED_WRONG_FACILITY);
                    break;
                }
                Shop shop = (Shop)facility;
                item = world.getItemOrTool(params.get(0));
                if(item == null){
                    entity.setLastActionResult(FAILED_UNKNOWN_ITEM);
                    break;
                }
                amount = -1;
                try{
                    amount = Integer.parseInt(params.get(1));
                } catch(NumberFormatException ignored){}
                if(amount < 1 || amount > shop.getItemCount(item)){
                    entity.setLastActionResult(FAILED_ITEM_AMOUNT);
                    break;
                }
                if(entity.getFreeSpace() < amount * item.getVolume()){
                    entity.setLastActionResult(FAILED_CAPACITY);
                    break;
                }
                int price = shop.buy(item, amount);
                entity.addItem(item, amount);
                world.getTeam(world.getTeamForAgent(agent)).subMoney(price);
                entity.setLastActionResult(SUCCESSFUL);
                break;

            case DELIVER_JOB: // 1 param (job)
                if(params.size() != 1){
                    entity.setLastActionResult(FAILED_WRONG_PARAM);
                    break;
                }
                Job job = world.getJob(params.get(0));
                if(job == null){
                    entity.setLastActionResult(FAILED_UNKNOWN_JOB);
                    break;
                }
                if(!job.isActive()){
                    entity.setLastActionResult(FAILED_JOB_STATUS);
                    break;
                }
                if (!entity.getLocation().inRange(job.getStorage().getLocation())){
                    entity.setLastActionResult(FAILED_LOCATION);
                    break;
                }

                if (job instanceof AuctionJob) {
                    AuctionJob auctionJob = (AuctionJob) job;
                    if (!auctionJob.isAssigned() || !auctionJob.getAuctionWinner().equals(world.getTeamForAgent(agent))){
                        entity.setLastActionResult(FAILED_JOB_STATUS);
                        break;
                    }
                }

                final int[] itemsUsed = {0};
                job.getRequiredItems().forEach((it, qty) -> {
                    int used = job.deliver(it, entity.getItemCount(it), world.getTeamForAgent(agent));
                    entity.removeItem(it, used);
                    itemsUsed[0] += used;
                });

                if (itemsUsed[0] > 0){
                    String teamName = world.getTeamForAgent(agent);
                    if (job.checkCompletion(teamName)) {
                        // add reward to completing team
                        int reward = job instanceof AuctionJob? ((AuctionJob)job).getLowestBid() : job.getReward();
                        world.getTeam(teamName).addMoney(reward);
                        // if job posted by another team, subtract payment
                        if (!job.getPoster().equals(JobData.POSTER_SYSTEM))
                            world.getTeam(job.getPoster()).subMoney(reward);
                        entity.setLastActionResult(SUCCESSFUL);
                        break;
                    } else {
                        entity.setLastActionResult(PARTIAL_SUCCESS);
                        break;
                    }
                } else {
                    entity.setLastActionResult(USELESS);
                    break;
                }

            case BID_FOR_JOB: // 2 params (job, price)
                if(params.size() != 2){
                    entity.setLastActionResult(FAILED_WRONG_PARAM);
                    break;
                }
                job = world.getJob(params.get(0));
                if(job == null){
                    entity.setLastActionResult(FAILED_UNKNOWN_JOB);
                    break;
                }
                price = -1;
                try{
                    price = Integer.parseInt(params.get(1));
                } catch(NumberFormatException ignored){}
                if(price < 0){
                    entity.setLastActionResult(FAILED_WRONG_PARAM);
                    break;
                }
                if(!(job instanceof AuctionJob)){
                    entity.setLastActionResult(FAILED_JOB_TYPE);
                    break;
                }
                AuctionJob auction = (AuctionJob) job;
                if(!(job.getStatus() == Job.JobStatus.AUCTION)){
                    entity.setLastActionResult(FAILED_JOB_STATUS);
                    break;
                }
                auction.bid(world.getTeam(world.getTeamForAgent(agent)), price);
                entity.setLastActionResult(SUCCESSFUL);
                break;

            case DUMP: // 2 params (item, amount)
                if(params.size() != 2){
                    entity.setLastActionResult(FAILED_WRONG_PARAM);
                    break;
                }
                facility = world.getFacilityByLocation(entity.getLocation());
                if (facility == null){
                    entity.setLastActionResult(FAILED_LOCATION);
                    break;
                }
                if (!(facility instanceof Dump)){
                    entity.setLastActionResult(FAILED_WRONG_FACILITY);
                    break;
                }
                item = world.getItemOrTool(params.get(0));
                if (item == null){
                    entity.setLastActionResult(FAILED_UNKNOWN_ITEM);
                    break;
                }
                amount = -1;
                try{
                    amount = Integer.parseInt(params.get(1));
                } catch(NumberFormatException ignored){}
                if(amount < 1 || amount > entity.getItemCount(item)){
                    entity.setLastActionResult(FAILED_ITEM_AMOUNT);
                    break;
                }
                entity.removeItem(item, amount);
                entity.setLastActionResult(SUCCESSFUL);
                break;

            case CHARGE: // no params
                if(params.size() != 0){
                    entity.setLastActionResult(FAILED_WRONG_PARAM);
                    break;
                }
                facility = world.getFacilityByLocation(entity.getLocation());
                if(facility == null){
                    entity.setLastActionResult(FAILED_LOCATION);
                    break;
                }
                if(!(facility instanceof ChargingStation)){
                    entity.setLastActionResult(FAILED_WRONG_FACILITY);
                    break;
                }
                entity.charge(((ChargingStation)facility).getRate());
                entity.setLastActionResult(SUCCESSFUL);
                break;

            case RECHARGE: // no params
                if(params.size() != 0){
                    entity.setLastActionResult(FAILED_WRONG_PARAM);
                    break;
                }
                entity.charge(world.getRechargeRate() + RNG.nextInt(world.getRechargeRate() + 1));
                entity.setLastActionResult(SUCCESSFUL);
                break;

            case CONTINUE:
            case SKIP:
                if (entity.getRoute() != null)
                    entity.setLastActionResult(entity.advanceRoute(world.getGotoCost())? SUCCESSFUL : FAILED_NO_ROUTE);
                else // nothing happens successfully
                    entity.setLastActionResult(SUCCESSFUL);
                break;

            case ABORT:
                entity.clearRoute();
                entity.setLastActionResult(SUCCESSFUL);
                break;

            case POST_JOB:
                if(params.size() < 5 || params.size() % 2 == 0){ // needs at least 5 parameters (and an odd number)
                    entity.setLastActionResult(FAILED_WRONG_PARAM);
                    break;
                }
                int reward = -1;
                int duration = -1;
                try{
                    reward = Integer.parseInt(params.get(0));
                    duration = Integer.parseInt(params.get(1));
                } catch (NumberFormatException ignored){}
                if(reward < 1 || duration < 1){
                    entity.setLastActionResult(FAILED_WRONG_PARAM);
                    break;
                }
                Facility fac = world.getFacility(params.get(2));
                if(fac == null || !(fac instanceof Storage)){
                    entity.setLastActionResult(FAILED_WRONG_FACILITY);
                    break;
                }
                // check if team has reached post limit
                String team = world.getTeamForAgent(agent);
                long postedJobs = world.getJobs().stream()
                        .filter(j -> j.getPoster().equals(team))
                        .filter(Job::isActive)
                        .count();
                if(postedJobs >= world.getPostJobLimit()){
                    entity.setLastActionResult(FAILED_JOB_STATUS);
                    break;
                }
                // check item requirements
                Map<Item, Integer> requirements = new HashMap<>();
                for (int i = 3; i < params.size(); i += 2){
                    item = world.getItemOrTool(params.get(i));
                    if(item == null){
                        entity.setLastActionResult(FAILED_UNKNOWN_ITEM);
                        break;
                    }
                    amount = -1;
                    try{
                        amount = Integer.parseInt(params.get(i + 1));
                    } catch(NumberFormatException ignored){}
                    if(amount < 1){
                        entity.setLastActionResult(FAILED_ITEM_AMOUNT);
                        break;
                    }
                    // if an item is listed multiple times, only one occurrence is used
                    requirements.put(item, amount);
                }
                // create job with the parsed details (starting next step)
                job = new Job(reward, (Storage) fac, stepNo + 1, stepNo + duration, world.getTeamForAgent(agent));
                requirements.entrySet().forEach(e -> job.addRequiredItem(e.getKey(), e.getValue()));
                world.addJob(job);
                break;

            case GATHER: // no params
                if(params.size() != 0){
                    entity.setLastActionResult(FAILED_WRONG_PARAM);
                    break;
                }
                facility = world.getFacilityByLocation(entity.getLocation());
                if(facility == null){
                    entity.setLastActionResult(FAILED_LOCATION);
                    break;
                }
                if(!(facility instanceof ResourceNode)){
                    entity.setLastActionResult(FAILED_WRONG_FACILITY);
                    break;
                }
                boolean gatherResult = ((ResourceNode) facility).gather();
                if(gatherResult){
                    if(entity.getFreeSpace() < ((ResourceNode) facility).getResource().getVolume()){
                        entity.setLastActionResult(FAILED_CAPACITY);
                        break;
                    }
                    entity.addItem(((ResourceNode) facility).getResource(), 1);
                    entity.setLastActionResult(SUCCESSFUL);
                }else{
                    entity.setLastActionResult(PARTIAL_SUCCESS);
                }

                break;

            default:
                entity.setLastAction(Action.STD_UNKNOWN_ACTION);
                entity.setLastActionResult(FAILED);
        }
    }

    /**
     * Sets things that can only be done after all actions have been processed.
     */
    void postProcess(){
        // set last action result for receiver agents
        world.getEntities().stream()
                .filter(r -> r.getLastAction().getActionType().equals(RECEIVE))
                .forEach(r -> r.setLastActionResult(receivers.contains(r)? SUCCESSFUL : FAILED_COUNTERPART));

        // handle assembly
        // assemblers and assistants are performing correct actions in the correct facility
        // agents are in the same workshop
        assemblers.forEach(assembler -> {
            Item item = world.getNonToolItem(assembler.getLastAction().getParameters().get(0));
            if(item == null){
                assembler.setLastActionResult(FAILED_UNKNOWN_ITEM);
                assistants.get(assembler).forEach(a -> a.setLastActionResult(FAILED_COUNTERPART));
            }
            else if(!item.needsAssembly()){
                assembler.setLastActionResult(FAILED_ITEM_TYPE);
                assistants.get(assembler).forEach(a -> a.setLastActionResult(FAILED_COUNTERPART));
            }
            else{ // item exists and can be assembled
                Set<Entity> assembleTeam = new HashSet<>(assistants.get(assembler));
                assembleTeam.add(assembler);
                Set<Tool> tools = item.getRequiredTools();
                boolean toolMissing = false;
                for (Tool tool : tools) {
                    boolean toolFound = false;
                    for (Entity entity : assembleTeam) {
                        if(entity.getItemCount(tool) > 0 && entity.getRole().getTools().contains(tool)){
                            toolFound = true;
                            break;
                        }
                    }
                    if(!toolFound){
                        toolMissing = true;
                        break;
                    }
                }
                if(toolMissing){
                    assembler.setLastActionResult(FAILED_TOOLS);
                    assistants.get(assembler).forEach(a -> a.setLastActionResult(FAILED_TOOLS));
                }
                else{ // all tools available, check regular items now
                    String assemblyResult = canBeAssembled(item, assembler, assistants.get(assembler), true);
                    if(assemblyResult.equals(SUCCESSFUL)){
                        assembler.setLastActionResult(SUCCESSFUL);
                        assistants.get(assembler).forEach(a -> a.setLastActionResult(SUCCESSFUL));
                    }
                    else{
                        assembler.setLastActionResult(assemblyResult);
                        assistants.get(assembler).forEach(a -> a.setLastActionResult(FAILED_COUNTERPART));
                    }
                }
            }
        });
    }

    /**
     * Checks if a team of entities has all necessary items (except tools) to assemble an item.
     * If called to apply changes, checks first whether changes can be applied in total
     * (so it does not need to be called to check that from the outside before)
     * @param item the item type to assemble
     * @param assembler the head assembler
     * @param assistants the assistant assemblers
     * @param applyChanges whether to apply the changes (i.e. remove parts and add product to head assembler)
     * @return the result of the assemble action, i.e. one of {@link #SUCCESSFUL}, {@link #FAILED_ITEM_TYPE},
     * {@link #FAILED_ITEM_AMOUNT}, {@link #FAILED_CAPACITY}
     */
    private String canBeAssembled(Item item, Entity assembler, Set<Entity> assistants, boolean applyChanges){
        if(!item.needsAssembly()) return FAILED_ITEM_TYPE;
        if(applyChanges){
            String dryRunResult = canBeAssembled(item, assembler, assistants, false);
            if(!dryRunResult.equals(SUCCESSFUL)) return dryRunResult;
        }
        int freedVolume = 0;
        for(Item part: item.getRequiredItems().keySet()){
            int needed = item.getRequiredItems().get(part);
            int take = Math.min(needed, assembler.getItemCount(part));
            if(applyChanges) assembler.removeItem(part, take);
            needed -= take;
            freedVolume += take * item.getVolume();
            if(needed > 0){
                for (Entity assistant : assistants) {
                    take = Math.min(needed, assistant.getItemCount(part));
                    if(applyChanges) assistant.removeItem(part, take);
                    needed -= take;
                    if (needed == 0) break;
                }
            }
            if (needed > 0) return FAILED_ITEM_AMOUNT;
        }
        if(item.getVolume() > assembler.getFreeSpace() + freedVolume) return FAILED_CAPACITY; // new item would not fit into head assembler
        if(applyChanges) assembler.addItem(item, 1);
        return SUCCESSFUL;
    }
}
