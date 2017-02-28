package massim.protocol;

import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Arrays;

/**
 * Used to exchange the complete world state.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({StaticWorldData.class, DynamicWorldData.class})
public abstract class WorldData {

    /**
     * For jaxb.
     */
    protected WorldData(){}

    /**
     * Parses the given XML document to a new WorldData object.
     * @param doc the XML message to parse
     * @param context specific world data classes that need to be known for unmarshalling
     * @return a world data object according to the XML file or null if sth. went wrong
     */
    public static WorldData parse(Document doc, Class... context) {
        if (doc == null) return null;
        try {
            Class[] contextClasses = Arrays.copyOf(context, context.length + 1);
            contextClasses[contextClasses.length - 1] = WorldData.class;
            JAXBContext jc = JAXBContext.newInstance(contextClasses);
            return (WorldData) jc.createUnmarshaller().unmarshal(doc);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a valid XML representation by marshalling all annotated fields.
     * @param context specific world data class that is marshalled
     * @return the XML document
     */
    public Document toXML(Class... context){
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Class[] contextClasses = Arrays.copyOf(context, context.length + 1);
            contextClasses[contextClasses.length - 1] = WorldData.class;
            JAXBContext jc = JAXBContext.newInstance(contextClasses);
            jc.createMarshaller().marshal(this, document);
            return document;
        } catch (ParserConfigurationException | JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }
}
