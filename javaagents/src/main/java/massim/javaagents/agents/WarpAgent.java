package massim.javaagents.agents;

import eis.iilang.Action;
import eis.iilang.Percept;
import massim.javaagents.MailService;

/**
 * This agent is intended to be used with the QuickTest.json config.
 * It assumes it can warp to any place with the goto action (since it moves incredibly fast).
 */
public class WarpAgent extends Agent{

    private String jobTaken;

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

        for (Percept p: getPercepts()){

            if(p.getName().contains("job")){
                say(p.toProlog());
            }

            switch(p.getName()){
                case "job":
                    say("We should do this " + p.toProlog());
                    break;
            }
        }

        if (jobTaken == null){

        }


        return new Action("skip");
    }

    @Override
    public void handleMessage(Percept message) {

    }
}
