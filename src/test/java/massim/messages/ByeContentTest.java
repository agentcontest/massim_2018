package massim.messages;

import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Testing the Bye Message.
 */
public class ByeContentTest {

    /**
     * Checks whether a message with bye content transforms to XML and back without losing information.
     */
    @Test
    public void transformsCorrectly(){
        long time = System.currentTimeMillis();
        Message msg = new Message(time, new ByeContent());
        Document doc = msg.toXML();
        Message msg2 = Message.parse(doc);

        assert(msg2 != null);
        assert(msg2.getContent() instanceof ByeContent);
        assert(msg2.getTimestamp() == time);
        assert(msg2.getType().equals(msg.getType()));
    }
}