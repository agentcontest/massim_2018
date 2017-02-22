package massim.protocol;

import massim.protocol.messagecontent.Action;
import massim.protocol.messagecontent.AuthRequest;
import massim.protocol.messagecontent.AuthResponse;
import massim.protocol.messagecontent.Bye;

import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * The content of a message following the MASSim protocol.
 */
@XmlSeeAlso({AuthRequest.class, AuthResponse.class, Bye.class, Action.class})
public abstract class MessageContent {

    /**
     * @return the type of the message/content (should be one of the types specified in {@link Message} class).
     */
    public abstract String getType();
}
