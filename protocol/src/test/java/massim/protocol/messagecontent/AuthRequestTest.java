package massim.protocol.messagecontent;

import massim.protocol.Message;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Test the Auth-Request message
 */
public class AuthRequestTest {

    @Test
    public void deAndSerializesAllData(){

        String user = "Bob";
        String password = "hello4";

        Message source = new Message(System.currentTimeMillis(), new AuthRequest(user, password));
        Document doc = source.toXML();
        Message deserialized = Message.parse(doc);

        assert(deserialized.getContent().getType().equals(source.getContent().getType()));
        assert(deserialized.getContent() instanceof AuthRequest);

        AuthRequest request = (AuthRequest) deserialized.getContent();

        assert request.getUsername().equals(user);
        assert request.getPassword().equals(password);
    }
}