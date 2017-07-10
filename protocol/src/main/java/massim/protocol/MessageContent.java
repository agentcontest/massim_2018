package massim.protocol;

import massim.protocol.messagecontent.*;

import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * The content of a message following the MASSim protocol.
 */
@XmlSeeAlso({AuthRequest.class, AuthResponse.class, Bye.class, Action.class, SimEnd.class})
public abstract class MessageContent {

    /**
     * @return the type of the message/content (should be one of the types specified in {@link Message} class).
     */
    public abstract String getType();
}
