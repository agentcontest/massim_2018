package massim.protocol.scenario.city.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * The data class for Mission jobs.
 */
@XmlRootElement(name = "mission")
@XmlAccessorType(XmlAccessType.NONE)
public class MissionData extends AuctionJobData {

    @XmlAttribute
    private String missionID;

    private MissionData(){
        super();
    }

    /**
     * Constructor.
     *
     * @param name           id of the job
     * @param storage        target storage
     * @param start          start step
     * @param end            end step
     * @param reward         max reward money
     * @param requiredItems  items required for job completion
     * @param fine           money to pay if unsuccessful
     * @param lowestBid      current lowest bid (may be null if there is none)
     * @param auctionTime    amount of steps for auction process
     * @param deliveredItems the items already delivered for this job
     * @param poster         the job's origin
     */
    public MissionData(String name, String storage, int start, int end, int reward, List<ItemAmountData> requiredItems,
                       int fine, Integer lowestBid, int auctionTime, List<CompletionData> deliveredItems, String poster,
                       String missionID) {
        super(name, storage, start, end, reward, requiredItems, fine, lowestBid, auctionTime, deliveredItems, poster);
        this.missionID = missionID;
    }

    /**
     * @return the ID for this mission (not to be confused with the job's name)
     */
    public String getMissionID(){
        return missionID;
    }
}
