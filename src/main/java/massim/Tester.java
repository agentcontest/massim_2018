package massim;

import massim.messages.AuthRequestContent;
import massim.messages.Message;
import massim.scenario.city.data.Role;
import massim.scenario.city.percept.CityInitialPercept;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.HashSet;

/**
 * Created in 2017.
 */
public class Tester {

    public static void main(String[] args){

        Message message = new Message(System.currentTimeMillis(), new AuthRequestContent("Bob", "geheim"));
        Document doc = message.toXML();
        System.out.println(transDoc(doc));
        message = Message.parse(doc);
        if (message != null) System.out.println(message.getType());

        CityInitialPercept percept = new CityInitialPercept("Bob", null);
//        percept.testM.put("Bob's Item", new Item("An item", 1000, new HashSet<>()));
        percept.role = new CityInitialPercept.RoleData(new Role("role of bob", 1, 10000, 5, new HashSet<>()));
        message = new Message(System.currentTimeMillis(), percept);
        doc = message.toXML(CityInitialPercept.class);
        System.out.println(transDoc(doc));
    }

    public static String transDoc(Document doc){
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString().replaceAll("\n|\r", "");
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return "no";
    }
}
