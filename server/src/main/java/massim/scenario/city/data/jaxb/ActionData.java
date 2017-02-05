package massim.scenario.city.data.jaxb;

import massim.messages.Action;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Vector;

/**
 * Holds data of an action (that was already executed).
 */
@XmlRootElement(name = "action")
@XmlAccessorType(XmlAccessType.NONE)
public class ActionData {

    @XmlAttribute
    private String type;

    @XmlAttribute
    private String result;

    @XmlElement
    private List<String> params;

    /**
     * For JAXB
     */
    private ActionData() {}

    /**
     * Constructor.
     * @param lastAction the action to store data of
     * @param lastActionResult the result of that action
     */
    public ActionData(Action lastAction, String lastActionResult) {
        type = lastAction.getActionType();
        result = lastActionResult;
        if(lastAction.getParameters().size() > 0) params = new Vector<>(lastAction.getParameters());
    }

    /**
     * @return the type of the action
     */
    public String getType() {
        return type;
    }

    /**
     * @return the result of the action
     */
    public String getResult() {
        return result;
    }

    /**
     * @return a list of the actions parameters or null in case of 0 parameters
     */
    public List<String> getParams() {
        return params;
    }
}
