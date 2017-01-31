package massim;

import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A generic action.
 * Annotated to support JAXB un/marshalling to/from a message XML document.
 * @author ta10
 */
@XmlRootElement(name = "message") //the whole message object
@XmlAccessorType(XmlAccessType.NONE) //use only annotated things for XML
public class Action {

    public final static String NO_ACTION = "noAction";
    private final static String UNKNOWN_ACTION = "unknownAction";
    private final static String RANDOM_FAIL = "randomFail";

    static final Action STD_NO_ACTION = new Action(NO_ACTION);
    public static final Action STD_UNKNOWN_ACTION = new Action(UNKNOWN_ACTION);
    public static final Action STD_RANDOM_FAIL_ACTION = new Action(RANDOM_FAIL);

    @XmlElement(name = "action")
    private String type;

    @XmlElement(name = "id")
    private int id = -1;

    @XmlElement(name = "p")
    private List<String> params = new LinkedList<>();

    private Action(String type, String... parameters){
        this.type = type;
        this.params.addAll(Arrays.asList(parameters));
    }

    /**
     * Parses the given XML document to a valid Action object.
     * @param doc the XML action to parse
     * @return an action object according to the XML file or {@link #STD_NO_ACTION} if sth. went wrong
     */
    static Action parse(Document doc) {
        try {
            JAXBContext jc = JAXBContext.newInstance(Action.class);
            return (Action) jc.createUnmarshaller().unmarshal(doc);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return STD_NO_ACTION;
    }

    /**
     * Creates an action message document, that would be expected by the server, containing the data of this action.
     * @return the XML document
     */
    public Document toMessage(){
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            JAXBContext jc = JAXBContext.newInstance(Action.class);
            jc.createMarshaller().marshal(this, document);
            return document;
        } catch (ParserConfigurationException | JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return the type/name of this action
     */
    public String getType(){
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
        return params;
    }
}
