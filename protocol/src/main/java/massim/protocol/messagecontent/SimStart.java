package massim.protocol.messagecontent;

import massim.protocol.Message;
import massim.protocol.MessageContent;

/**
 * An abstract SIM-START message content. Needs to be subclassed in specific scenarios
 * and use JAXB annotations for XML serialization.
 */
public abstract class SimStart extends MessageContent {

    @Override
    public String getType(){
        return Message.TYPE_SIM_START;
    }
}
