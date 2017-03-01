package massim.protocol.messagecontent;

import massim.protocol.Message;
import massim.protocol.MessageContent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An AUTHENTICATION-RESPONSE message for Server-2-Agent communication.
 */
@XmlRootElement(name = "auth-response")
@XmlAccessorType(XmlAccessType.NONE) //use only annotated things for XML
public class AuthResponse extends MessageContent {

    @XmlAttribute(name="result")
    private String result;

    /**
     * Private constructor only to be used by JAXB.
     */
    private AuthResponse(){}

    public AuthResponse(AuthenticationResult result){
        switch (result) {
            case OK:
                this.result = "ok";
                break;
            case FAILED:
                this.result = "fail";
                break;
            default:
                this.result = "fail";
        }
    }

    @Override
    public String getType() {
        return Message.TYPE_AUTH_RESPONSE;
    }

    public AuthenticationResult getResult(){
        if(result == null) return AuthenticationResult.FAILED;
        switch(result){
            case "ok": return AuthenticationResult.OK;
            case "failed": return AuthenticationResult.FAILED;
            default: return AuthenticationResult.FAILED;
        }
    }

    /**
     * Contains all possible results of an authentication.
     */
    public enum AuthenticationResult{
        OK, FAILED
    }
}
