package massim.javaagents.agents;

import eis.iilang.*;
import massim.javaagents.MailService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This agent tries to post a weird job in each step. Also, it bids on an auction if there is one.
 */
public class JobPostingAgent extends Agent{

    private Random random = new Random();
    private Percept storage;
    private Map<String, Percept> items = new HashMap<>();

    /**
     * Constructor
     *
     * @param name    the agent's name
     * @param mailbox the mail facility
     */
    public JobPostingAgent(String name, MailService mailbox) {
        super(name, mailbox);
    }

    @Override
    public void handlePercept(Percept percept) {}

    @Override
    public Action step() {
        List<Percept> percepts = getPercepts();

//        say(percepts.stream()
//                .map(Percept::toProlog)
//                .collect(Collectors.joining("\n")));

        for(Percept percept: percepts){
            switch(percept.getName()){
                case "storage":
                    storage = percept; // remember any one storage
                    break;
                case "item":
                    items.putIfAbsent(percept.getName(), percept);
                    break;
                case "auction":
                    return new Action("bid_for_job", new Identifier(WarpAgent.stringParam(percept.getParameters(), 0)), new Numeral(20));
            }
        }

        if(storage != null && items.keySet().size() > 0){
            // storage and items are known, so we can post a job
            int reward = 5000 + random.nextInt(5000);
            int duration = 100 + random.nextInt(100);
            Parameter item1 = items.values().iterator().next().getParameters().get(0);

            return new Action("post_job",
                    new Numeral(reward),
                    new Numeral(duration),
                    storage.getParameters().get(0), //storage.name
                    new Identifier("item0"),
                    new Numeral(3)
            );
        }
        else{
            return new Action("skip");
        }
    }

    @Override
    public void handleMessage(Percept message, String sender) {} // this agent does not care what others have to say
}
