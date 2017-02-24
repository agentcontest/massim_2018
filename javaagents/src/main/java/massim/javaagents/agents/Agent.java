package massim.javaagents.agents;

import eis.iilang.Percept;
import massim.javaagents.MailBox;
import massim.protocol.messagecontent.Action;

import java.util.List;
import java.util.Vector;

/**
 * An abstract Java agent.
 */
public abstract class Agent {

    private String name;
    private MailBox mailbox;
    private List<Percept> percepts = new Vector<>();

    /**
     * Constructor
     * @param name the agent's name
     * @param mailbox the mail facility
     */
    Agent(String name, MailBox mailbox){
        this.name = name;
        this.mailbox = mailbox;
    }

    /**
     * Handles a percept.
     * This method is used only if the EIS is configured to handle percepts as notifications.
     * @param percept the percept to process
     */
    public abstract void handlePercept(Percept percept);

    /**
     * Called for each step.
     */
    public abstract Action step();

    /**
     * @return the name of the agent
     */
    public String getName() {
        return name;
    }

    /**
     * Sends a percept as a message to the given agent.
     * Messages will be delivered after all agents have been stepped.
     * @param message the message to deliver
     * @param receiver the receiving agent
     */
    protected void sendMessage(Percept message, String receiver){
        mailbox.sendMessage(message, receiver);
    }

    /**
     * @return a list of all messages that were sent to this agent
     */
    protected List<Percept> receiveMessages(){
        return mailbox.getMessages(name);
    }

    /**
     * Sets the percepts for this agent. Should only be called from the outside.
     * @param percepts the new percepts for this agent.
     */
    public void setPercepts(List<Percept> percepts) {
        this.percepts = percepts;
    }

    /**
     * Prints a message to std out prefixed with the agent's name.
     * @param message the message to say
     */
    protected void say(String message){
        System.out.println("[ " + name + " ]  " + message);
    }

    /**
     * Returns a list of this agent's percepts. Percepts are set by the scheduler
     * each time before the step() method is called.
     * Percepts are cleared before each step, so relevant information needs to be stored somewhere else
     * by the agent.
     * @return a list of all new percepts for the current step
     */
    List<Percept> getPercepts(){
        return percepts;
    }
}
