package massim.javaagents;

import eis.iilang.Percept;
import massim.javaagents.agents.Agent;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A simple register for agents that forwards messages.
 */
public class MailService {

    private Map<String, Agent> register = new HashMap<>();
    private Logger logger = Logger.getLogger("agents");

    /**
     * Registers an agent with this mail service. The agent will now receive messages.
     * @param agent the agent to register
     */
    public void registerAgent(Agent agent){
        register.put(agent.getName(), agent);
    }

    /**
     * Adds a message to this mailbox.
     * @param message the message to add
     * @param to the receiving agent
     */
    public void sendMessage(Percept message, String to){

        Agent recipient = register.get(to);

        if(recipient == null) {
            logger.warning("Cannot deliver message to " + to + "; unknown target,");
        }
        else{
            recipient.handleMessage(message);
        }
    }
}
