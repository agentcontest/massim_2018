package massim.protocol.messagecontent;

import massim.protocol.Message;
import massim.protocol.MessageContent;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The content of a bye message in the MASSim protocol. There is no content besides timestamp and type.
 */
@XmlRootElement(name="bye")
public class Bye extends MessageContent {

    @Override
    public String getType() {
        return Message.TYPE_BYE;
    }
}
