package massim.javaagents.agents;

import eis.iilang.*;
import massim.javaagents.MailService;

import java.util.*;

/**
 * This agent is intended to be used with the QuickTest.json config.
 * It assumes it can warp to any place with the goto action (since it moves incredibly fast).
 * Also, it plans very statically and may break easily (because of that).
 * Also, it cannot assemble yet.
 */
public class WarpAgent extends Agent{

    private Set<String> jobsTaken = new HashSet<>();
    private String myJob;

    private Queue<Action> actionQueue = new LinkedList<>();

    private boolean test = false;

    /**
     * Constructor.
     *
     * @param name    the agent's name
     * @param mailbox the mail facility
     */
    public WarpAgent(String name, MailService mailbox) {
        super(name, mailbox);
    }

    @Override
    public void handlePercept(Percept percept) {} // this is not configured to be called

    @Override
    public Action step() {

        Map<String, Percept> currentJobs = new HashMap<>();
        Map<String, List<Percept>> shopsByItem = new HashMap<>();

        String lastAction = "";
        String carriedItems = "";
        Vector<String> resourceNodes = new Vector<>();
        String lon = "";
        String lat = "";

        for (Percept p: getPercepts()){
            switch(p.getName()){
                // parse info that is always needed
                case "lastAction":
                case "lastActionParams":
                case "lastActionResult":
                    lastAction += " " + p.toProlog();
                    break;
                case "item":
                    carriedItems += " " + p.toProlog();
                    break;
                case "resourceNode":
                    resourceNodes.add(p.toProlog());
                    break;
                case "lon":
                    lon = p.toProlog();
                    break;
                case "lat":
                    lat = p.toProlog();
                    break;
            }
            if(actionQueue.size() == 0){
                // parse info needed for planning
                switch(p.getName()){
                    case "job":
                        // remember all active jobs
                        currentJobs.putIfAbsent(stringParam(p.getParameters(), 0), p);
                        break;
                    case "shop":
                        // remember shops by what they offer
                        ParameterList stockedItems = listParam(p, 4);
                        for(Parameter stock: stockedItems){
                            if(stock instanceof Function){
                                String itemName = stringParam(((Function) stock).getParameters(), 0);
                                int amount = intParam(((Function) stock).getParameters(), 2);
                                if(amount > 0){
                                    shopsByItem.putIfAbsent(itemName, new ArrayList<>());
                                    shopsByItem.get(itemName).add(p);
                                }
                            }
                        }
                        break;
                }
            }
        }

        say("Last step I did " + lastAction);

        if(carriedItems.isEmpty()==false){
            say("I carry " + carriedItems);
        }

        say("I am at " + lon + " " + lat);

        if(resourceNodes.isEmpty()==false){
            for(String nodeInfo: resourceNodes){
                say(nodeInfo);
            }
        }

        //test gather action
        if(test==false){
            actionQueue.add(new Action("goto", new Identifier("resourceNode2")));
            actionQueue.add(new Action("gather"));
            test=true;
        }

        // follow the plan if there is one
        if(actionQueue.size() > 0) return actionQueue.poll();

        if (myJob == null){
            Set<String> availableJobs = new HashSet<>(currentJobs.keySet());
            availableJobs.removeAll(jobsTaken);
            if(availableJobs.size() > 0){
                myJob = availableJobs.iterator().next();
                say("I will complete " + myJob);
                jobsTaken.add(myJob);
                broadcast(new Percept("taken", new Identifier(myJob)), getName());
            }
        }
        if(myJob != null){
            // plan the job
            // 1. acquire items
            Percept job = currentJobs.get(myJob);
            if(job == null){
                say("I lost my job :(");
                myJob = null;
                return new Action("skip");
            }
            String storage = stringParam(job.getParameters(), 1);
            ParameterList requirements = listParam(job, 4);
            for (Parameter requirement : requirements) {
                if(requirement instanceof Function){
                    // 1.1 get enough items of that type
                    String itemName = stringParam(((Function) requirement).getParameters(), 0);
                    int amount = intParam(((Function) requirement).getParameters(), 1);
                    if(itemName.equals("") || amount == -1){
                        say("Something is wrong with this item: " + itemName + " " + amount);
                        continue;
                    }
                    // find a shop selling the item
                    List<Percept> shops = shopsByItem.get(itemName);
                    if(shops.size() == 0){
                        say("I cannot buy the item " + itemName + "; this plan won't work very well.");
                    }
                    else{
                        say("I will go to the shop first.");
                        // go to the shop
                        Percept shop = shops.get(0);
                        actionQueue.add(new Action("goto", new Identifier(stringParam(shop.getParameters(), 0))));
                        // buy the items
                        actionQueue.add(new Action("buy", new Identifier(itemName), new Numeral(amount)));
                    }
                }
            }
            // 2. get items to storage
            actionQueue.add(new Action("goto", new Identifier(storage)));
            // 2.1 deliver items
            actionQueue.add(new Action("deliver_job", new Identifier(myJob)));
        }

        return actionQueue.peek() != null? actionQueue.poll() : new Action("skip");
    }

    @Override
    public void handleMessage(Percept message, String sender) {
        switch (message.getName()){
            case "taken":
                jobsTaken.add(stringParam(message.getParameters(), 0));
                break;
        }
    }

    /**
     * Tries to extract a parameter from a list of parameters.
     * @param params the parameter list
     * @param index the index of the parameter
     * @return the string value of that parameter or an empty string if there is no parameter or it is not an identifier
     */
    public static String stringParam(List<Parameter> params, int index){
        if(params.size() < index + 1) return "";
        Parameter param = params.get(index);
        if(param instanceof Identifier) return ((Identifier) param).getValue();
        return "";
    }

    /**
     * Tries to extract an int parameter from a list of parameters.
     * @param params the parameter list
     * @param index the index of the parameter
     * @return the int value of that parameter or -1 if there is no parameter or it is not an identifier
     */
    private static int intParam(List<Parameter> params, int index){
        if(params.size() < index + 1) return -1;
        Parameter param = params.get(index);
        if(param instanceof Numeral) return ((Numeral) param).getValue().intValue();
        return -1;
    }

    /**
     * Tries to extract a parameter from a percept.
     * @param p the percept
     * @param index the index of the parameter
     * @return the string value of that parameter or an empty string if there is no parameter or it is not an identifier
     */
    private static ParameterList listParam(Percept p, int index){
        List<Parameter> params = p.getParameters();
        if(params.size() < index + 1) return new ParameterList();
        Parameter param = params.get(index);
        if(param instanceof ParameterList) return (ParameterList) param;
        return new ParameterList();
    }
}
