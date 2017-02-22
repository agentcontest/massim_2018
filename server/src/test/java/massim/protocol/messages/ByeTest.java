package massim.protocol.messages;

import massim.protocol.messagecontent.Bye;
import massim.protocol.Message;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Testing the Bye Message.
 */
public class ByeTest {

    /**
     * Checks whether a message with bye content transforms to XML and back without losing information.
     */
    @Test
    public void transformsCorrectly(){
        long time = System.currentTimeMillis();
        Message msg = new Message(time, new Bye());
        Document doc = msg.toXML();
        Message msg2 = Message.parse(doc);

        assert(msg2 != null);
        assert(msg2.getContent() instanceof Bye);
        assert(msg2.getTimestamp() == time);
        assert(msg2.getType().equals(msg.getType()));
    }
}