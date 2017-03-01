package massim.protocol.messagecontent;

import massim.protocol.Message;
import massim.protocol.MessageContent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An AUTHENTICATION-REQUEST message for Agent-2-Server communication.
 */
@XmlRootElement(name = "auth-request")
@XmlAccessorType(XmlAccessType.NONE) //use only annotated things for XML
public class AuthRequest extends MessageContent {

    @XmlAttribute(name="username")
    private String username;

    @XmlAttribute(name="password")
    private String password;

    /**
     * private constructor only intended for use by JAXB
     */
    private AuthRequest(){}

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    @Override
    public String getType() {
        return Message.TYPE_AUTH_REQUEST;
    }
}
