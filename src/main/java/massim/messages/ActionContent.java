package massim.messages;

import javax.xml.bind.annotation.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A generic action.
 * Annotated to support JAXB un/marshalling to/from a message XML document.
 * @author ta10
 */
@XmlRootElement(name = "action") //the whole message object
@XmlAccessorType(XmlAccessType.NONE) //use only annotated things for XML
public class ActionContent extends MessageContent{

    public final static String NO_ACTION = "noAction";
    private final static String UNKNOWN_ACTION = "unknownAction";
    private final static String RANDOM_FAIL = "randomFail";

    public static final ActionContent STD_NO_ACTION = new ActionContent(NO_ACTION);
    public static final ActionContent STD_UNKNOWN_ACTION = new ActionContent(UNKNOWN_ACTION);
    public static final ActionContent STD_RANDOM_FAIL_ACTION = new ActionContent(RANDOM_FAIL);

    @XmlAttribute(name = "type")
    private String type;

    @XmlAttribute(name = "id")
    private int id = -1;

    @XmlElement(name = "p")
    private List<String> params;

    /**
     * For JAXB
     */
    private ActionContent(){}

    /**
     * Constructor.
     * @param type the actions' type
     * @param parameters parameters of the action
     */
    public ActionContent(String type, String... parameters){
        this.type = type;
        if(parameters.length > 0){
            params = new LinkedList<>();
            params.addAll(Arrays.asList(parameters));

        }
    }

    @Override
    public String getType(){
        return Message.TYPE_ACTION;
    }

    /**
     * @return the action type (e.g. "goto")
     */
    public String getActionType(){
        return type;
    }

    /**
     * Setter for the action-id.
     * Only used when building an XML message out of this object.
     * @param id an action id from a request-action message
     */
    public void setID(int id){
        this.id = id;
    }

    /**
     * @return the parameter(s) of this action (in a possibly empty list)
     */
    public List<String> getParameters(){
        return params == null? new LinkedList<>() : params;
    }
}