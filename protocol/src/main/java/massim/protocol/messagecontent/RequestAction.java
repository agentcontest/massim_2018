package massim.protocol.messagecontent;

import massim.protocol.Message;
import massim.protocol.MessageContent;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A percept that is sent to an agent before each step.
 * Needs to be subclassed for each scenario.
 * Subclass needs to support JAXB annotations to be de/serialized via XML.
 */
@XmlRootElement
public abstract class RequestAction extends MessageContent {

    @XmlAttribute
    private long id;

    @XmlAttribute
    private long deadline;

    @Override
    public String getType(){
        return Message.TYPE_REQUEST_ACTION;
    }

    /**
     * @return the action-id of this percept/request-action message
     */
    public long getId(){
        return id;
    }

    /**
     * @return the server time in ms for the agent to respond at the latest to this message
     */
    public long getDeadline(){
        return deadline;
    }

    /**
     * Adds action-id and deadline to this request-action percept.
     * @param id the action-id
     * @param deadline the server time in ms for the agent to respond at the latest
     */
    public void finalize(long id, long deadline) {
        this.id = id;
        this.deadline = deadline;
    }
}
