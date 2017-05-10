package massim.eismassim.entities;

import eis.iilang.*;
import eis.iilang.Action;
import massim.eismassim.EISEntity;
import massim.protocol.Message;
import massim.protocol.messagecontent.*;
import massim.protocol.scenario.city.data.*;
import massim.protocol.scenario.city.percept.CityInitialPercept;
import massim.protocol.scenario.city.percept.CityStepPercept;
import org.w3c.dom.Document;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An EIS compatible entity for the 2017 MAPC City scenario.
 */
public class CityEntity extends EISEntity {

    /**
     * Creates a new CityEntity. To create a new one, call factory method
     * {@link EISEntity#createEntity(String, String, String, int, String, String)}
     */
    public CityEntity() {}

    @Override
    protected Class[] getPerceptTypes() {
        return new Class[]{CityInitialPercept.class, CityStepPercept.class};
    }

    @Override
    protected List<Percept> simStartToIIL(SimStart startPercept) {

        List<Percept> ret = new Vector<>();
        if(!(startPercept instanceof CityInitialPercept)) return ret; // protocol incompatibility
        CityInitialPercept simStart = (CityInitialPercept) startPercept;

        // add global info
        ret.add(new Percept("id", new Identifier(simStart.getId())));
        ret.add(new Percept("map", new Identifier(simStart.getMapName())));
        ret.add(new Percept("seedCapital", new Numeral(simStart.getSeedCapital())));
        ret.add(new Percept("steps", new Numeral(simStart.getSteps())));
        ret.add(new Percept("team", new Identifier(simStart.getTeam())));

        // prepare list of tools
        RoleData role = simStart.getRoleData();
        ParameterList paramTools = new ParameterList();
        if(role.getTools() != null)
            role.getTools().forEach(tool -> paramTools.add(new Identifier(tool)));

        // add role percept
        ret.add(new Percept("role",
                new Identifier(role.getName()), new Numeral(role.getSpeed()), new Numeral(role.getLoad()),
                new Numeral(role.getBattery()), paramTools));

        // add item percepts
        simStart.getItemData().forEach(item -> {
            LinkedList<Parameter> params = new LinkedList<>();
            params.add(new Identifier(item.getName()));
            params.add(new Numeral(item.getVolume()));

            ParameterList requiredTools = new ParameterList();
            if(item.getTools() != null)
                item.getTools().forEach(tool -> requiredTools.add(new Identifier(tool)));
            params.add(new Function("tools", requiredTools));

            ParameterList requiredParts = new ParameterList();
            item.getParts().forEach(part ->
                    requiredParts.add(new ParameterList(new Identifier(part.getName()), new Numeral(part.getAmount()))));
            params.add(new Function("parts", requiredParts));

            ret.add(new Percept("item", params));
        });

        return ret;
    }

    @Override
    protected Collection<Percept> requestActionToIIL(Message message) {
        Set<Percept> ret = new HashSet<>();
        if(!(message.getContent() instanceof CityStepPercept)) return ret; // percept incompatible with entity
        CityStepPercept percept = (CityStepPercept) message.getContent();

        ret.add(new Percept("actionID", new Numeral(percept.getId())));
        ret.add(new Percept("timestamp", new Numeral(message.getTimestamp())));
        ret.add(new Percept("deadline", new Numeral(percept.getDeadline())));
        ret.add(new Percept("step", new Numeral(percept.getSimData().getStep())));

        EntityData self = percept.getSelfData();
        ret.add(new Percept("charge", new Numeral(self.getCharge())));
        ret.add(new Percept("load", new Numeral(self.getLoad())));
        ret.add(new Percept("lat", new Numeral(self.getLat())));
        ret.add(new Percept("lon", new Numeral(self.getLon())));
        ret.add(new Percept("routeLength", new Numeral(self.getRouteLength())));
        ret.add(new Percept("money", new Numeral(percept.getTeamData().getMoney())));

        if(self.getFacility() != null) ret.add(new Percept("facility", new Identifier(self.getFacility())));

        // add percepts for last action
        ActionData lastAction = self.getLastAction();
        ret.add(new Percept("lastAction", new Identifier(lastAction.getType())));
        ParameterList lastActionParams = new ParameterList();
        if(lastAction.getParams() != null)
            lastAction.getParams().forEach(param -> lastActionParams.add(new Identifier(param)));
        ret.add(new Percept("lastActionParams", lastActionParams));
        ret.add(new Percept("lastActionResult", new Identifier(lastAction.getResult())));

        // add carried items
        self.getItems().forEach(item -> ret.add(
                new Percept("hasItem", new Identifier(item.getName()), new Numeral(item.getAmount()))));

        // add waypoints if route exists
        if(self.getRoute() != null) {
            ParameterList waypoints = new ParameterList();
            self.getRoute().forEach(wp -> waypoints.add(new Function(
                    "wp", new Numeral(wp.getIndex()), new Numeral(wp.getLat()), new Numeral(wp.getLon()))));
            ret.add(new Percept("route", waypoints));
        }

        // add percept for each entity
        percept.getEntityData().forEach(entity -> ret.add(new Percept("entity",
                new Identifier(entity.getName()), new Identifier(entity.getTeam()), new Numeral(entity.getLat()),
                new Numeral(entity.getLon()), new Identifier(entity.getRole()))));

        // add charging station percepts
        percept.getChargingStations().forEach(ch -> ret.add(new Percept("chargingStation",
                new Identifier(ch.getName()), new Numeral(ch.getLat()),
                new Numeral(ch.getLon()), new Numeral(ch.getRate()))));

        percept.getDumps().forEach(dump -> ret.add(new Percept("dump", new Identifier(dump.getName()),
                new Numeral(dump.getLat()), new Numeral(dump.getLon()))));

        // shop percepts
        percept.getShopData().forEach(shop -> {
            ParameterList shopItems = new ParameterList(
                    shop.getOfferedItems().stream() // map items to functions and collect those in a param-list
                            .map(item -> new Function("item", new Identifier(item.getName()),
                                    new Numeral(item.getPrice()), new Numeral(item.getAmount())))
                            .collect(Collectors.toList()));
            ret.add(new Percept("shop", new Identifier(shop.getName()), new Numeral(shop.getLat()),
                    new Numeral(shop.getLon()), new Numeral(shop.getRestock()), shopItems));
        });

        // storage percepts
        percept.getStorage().forEach(storage -> {
            // map items in the storage to Functions
            ParameterList storageItems = new ParameterList(
                storage.getStoredItems().stream().map(item -> new Function("item", new Identifier(item.getName()),
                        new Numeral(item.getStored()), new Numeral(item.getDelivered()))).collect(Collectors.toList()));
            ret.add(new Percept("storage", new Identifier(storage.getName()), new Numeral(storage.getLat()),
                    new Numeral(storage.getLon()), new Numeral(storage.getTotalCapacity()),
                    new Numeral(storage.getUsedCapacity()), storageItems));
        });

        // workshop percepts
        percept.getWorkshops().forEach(ws -> ret.add(new Percept("workshop", new Identifier(ws.getName()),
                new Numeral(ws.getLat()), new Numeral(ws.getLon()))));

        // resourceNode percepts
        percept.getResourceNodes().forEach(node -> ret.add(new Percept("resourceNode", new Identifier(node.getName()),
                new Numeral(node.getLat()), new Numeral(node.getLon()), new Identifier(node.getResource()))));

        // job percepts
        percept.getJobs().forEach(job -> ret.add(createJobPercept(job, "job")));
        percept.getAuctions().forEach(job -> ret.add(createJobPercept(job, "auction")));
        percept.getPostedJobs().forEach(job -> ret.add(createJobPercept(job, "posted")));
        percept.getMissions().forEach(job -> ret.add(createJobPercept(job, "mission")));

        return ret;
    }

    /**
     * Creates a job percept based on a {@link JobData} object.
     * @param job the source job data
     * @param name the name of the percept to create
     * @return a Percept for the job
     */
    private static Percept createJobPercept(JobData job, String name){
        Percept jobPercept = new Percept(name, new Identifier(job.getId()), new Identifier(job.getStorage()),
                new Numeral(job.getReward()), new Numeral(job.getStart()), new Numeral(job.getEnd()));
        if(job instanceof AuctionJobData){
            // add auction data
            AuctionJobData auction = (AuctionJobData) job;
            Integer lowestBid = auction.getLowestBid();
            if (lowestBid == null) lowestBid = 0;
            jobPercept.addParameter(new Numeral(auction.getFine()));
            jobPercept.addParameter(new Numeral(lowestBid));
            jobPercept.addParameter(new Numeral(auction.getAuctionTime()));
            if(job instanceof MissionData){
                jobPercept.addParameter(new Identifier(((MissionData) job).getMissionID()));
            }
        }
        // add item data
        ParameterList requiredItems = new ParameterList();
        job.getRequiredItems().forEach(item -> requiredItems.add(
                new Function("required", new Identifier(item.getName()), new Numeral(item.getAmount()))));
        jobPercept.addParameter(requiredItems);
        return jobPercept;
    }

    @Override
    protected Collection<Percept> simEndToIIL(SimEnd endPercept) {
        HashSet<Percept> ret = new HashSet<>();
        if (endPercept != null){
            ret.add(new Percept("ranking", new Numeral(endPercept.getRanking())));
            ret.add(new Percept("score", new Numeral(endPercept.getScore())));
        }
        return ret;
    }

    @Override
    public Document actionToXML(Action action) {

        // translate parameters to String
        List<String> parameters = new Vector<>();
        action.getParameters().forEach(param -> {
            if (param instanceof Identifier){
                parameters.add(((Identifier) param).getValue());
            }
            else if(param instanceof Numeral){
                parameters.add(((Numeral) param).getValue().toString());
            }
            else{
                log("Cannot translate parameter " + param);
                parameters.add(""); // add empty parameter so the order is not invalidated
            }
        });

        // create massim protocol action
        massim.protocol.messagecontent.Action massimAction =
                new massim.protocol.messagecontent.Action(action.getName(), parameters.toArray(new String[parameters.size()]));
        massimAction.setID(currentActionId);

        return new Message(null, massimAction).toXML();
    }
}
