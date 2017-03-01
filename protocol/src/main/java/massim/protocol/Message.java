package massim.protocol;

import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Arrays;

/**
 * A message received or to be sent by the MASSim server.
 */
@XmlRootElement(name = "message") //the whole message object
@XmlAccessorType(XmlAccessType.NONE) //use only annotated things for XML
public class Message {

    public final static String TYPE_REQUEST_ACTION = "request-action";
    public final static String TYPE_ACTION = "action";
    public final static String TYPE_AUTH_REQUEST = "auth-request";
    public final static String TYPE_AUTH_RESPONSE = "auth-response";
    public final static String TYPE_SIM_START = "sim-start";
    public final static String TYPE_SIM_END = "sim-end";
    public final static String TYPE_BYE = "bye";

    @XmlAttribute(name = "timestamp")
    private Long timestamp;

    @XmlAttribute(name = "type")
    private String type;

    @XmlElementRef
    private MessageContent content;

    /**
     * Private constructor only to be used by JAXB.
     */
    @SuppressWarnings("unused")
    private Message(){}

    /**
     * Creates a new message of the given type. Timestamp may be null (for agent-2-server messages).
     * @param timestamp the time of the message, may be null (see above).
     * @param content a message content object specifying the message type and specific contents
     */
    public Message(Long timestamp, MessageContent content){
        this.type = content.getType();
        if(timestamp != null) this.timestamp = timestamp;
        this.content = content;
    }

    /**
     * @return the message's type
     */
    public String getType(){
        return type;
    }

    /**
     * @return the message's timestamp or -1 if it does not have one
     */
    public Long getTimestamp(){
        if(timestamp == null) return -1L;
        return timestamp;
    }

    /**
     * Parses the given XML document to a valid message object.
     * @param doc the XML message to parse
     * @param context specific message content classes used for this message
     * @return a message object according to the XML file or null if sth. went wrong
     */
    public static Message parse(Document doc, Class... context) {
        if (doc == null) return null;
        try {
            Class[] contextClasses = Arrays.copyOf(context, context.length + 1);
            contextClasses[contextClasses.length - 1] = Message.class;
            JAXBContext jc = JAXBContext.newInstance(contextClasses);
            return (Message) jc.createUnmarshaller().unmarshal(doc);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a valid message document marshalling all annotated fields.
     * @param context specific message content classes used for this message
     * @return the XML document
     */
    public Document toXML(Class... context){
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Class[] contextClasses = Arrays.copyOf(context, context.length + 2);
            contextClasses[contextClasses.length - 2] = Message.class;
            contextClasses[contextClasses.length - 1] = getContent().getClass();
            JAXBContext jc = JAXBContext.newInstance(contextClasses);
            jc.createMarshaller().marshal(this, document);
            return document;
        } catch (ParserConfigurationException | JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return the variable content of this message
     */
    public MessageContent getContent(){
        return content != null? content : new DefaultMessageContent();
    }

    /**
     * Used if no valid message content could be deserialized.
     */
    public class DefaultMessageContent extends MessageContent{
        @Override
        public String getType() {
            return "DefaultMessageContent";
        }
    }
}