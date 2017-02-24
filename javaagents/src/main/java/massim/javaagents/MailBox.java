package massim.javaagents;

import eis.iilang.Percept;

import java.util.*;

/**
 * Created in 2017.
 */
public class MailBox {

    private Map<String, List<Percept>> mail = new HashMap<>();

    /**
     * Adds a message to this mailbox.
     * @param message the message to add
     * @param receiver the receiving agent
     */
    public synchronized void sendMessage(Percept message, String receiver){
        mail.putIfAbsent(receiver, new Vector<>());
        mail.get(receiver).add(message);
    }

    /**
     * Retrieves and removes all messages for the given agent.
     * @param agent the agent to get the mail for
     * @return a list of all messages for the given agent
     */
    public synchronized List<Percept> getMessages(String agent){
        List<Percept> messages = mail.get(agent);
        mail.put(agent, new Vector<>());
        return messages != null? messages: new Vector<>();
    }
}
