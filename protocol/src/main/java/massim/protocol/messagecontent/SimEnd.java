package massim.protocol.messagecontent;

import massim.protocol.Message;
import massim.protocol.MessageContent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A SIM-END message content for Server-2-Agent communication.
 */
@XmlRootElement(name = "sim-result")
@XmlAccessorType(XmlAccessType.NONE) //use only annotated things for XML
public class SimEnd extends MessageContent {

    @XmlAttribute
    private int ranking;

    @XmlAttribute
    private long score;

    /**
     * JAXB private constructor
     */
    private SimEnd(){}

    public SimEnd(int ranking, long score){
        this.ranking = ranking;
        this.score = score;
    }

    @Override
    public String getType() {
        return Message.TYPE_SIM_END;
    }

    /**
     * @return the team's ranking
     */
    public int getRanking(){
        return ranking;
    }

    /**
     * @return the team's score
     */
    public long getScore(){
        return score;
    }
}
