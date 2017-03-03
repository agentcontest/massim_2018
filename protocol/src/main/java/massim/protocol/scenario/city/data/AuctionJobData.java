package massim.protocol.scenario.city.data;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.List;

/**
 * Holds the data of an auction job.
 */
public class AuctionJobData extends JobData {

    @XmlAttribute
    private int fine;

    @XmlAttribute
    private int auctionTime;

    @XmlAttribute
    private Integer lowestBid;

    /**
     * For jaxb
     */
    private AuctionJobData(){}

    /**
     * Constructor.
     * @param name id of the job
     * @param storage target storage
     * @param end end step
     * @param reward max reward money
     * @param requiredItems items required for job completion
     * @param fine money to pay if unsuccessful
     * @param lowestBid current lowest bid (may be null if there is none)
     * @param auctionTime amount of steps for auction process
     * @param poster the job's origin
     */
    public AuctionJobData(String name, String storage, int end, int reward, List<ItemAmountData> requiredItems,
                          int fine, Integer lowestBid, int auctionTime, List<CompletionData> deliveredItems,
                          String poster) {
        super(name, storage, end, reward, requiredItems, deliveredItems, poster);
        this.fine = fine;
        this.lowestBid = lowestBid;
        this.auctionTime = auctionTime;
    }

    /**
     * @return the auction's fine
     */
    public int getFine(){
        return fine;
    }

    /**
     * @return the lowest bid for this auction (may be null)
     */
    public Integer getLowestBid(){
        return lowestBid;
    }

    /**
     * @return the auctioning duration
     */
    public int getAuctionTime(){
        return auctionTime;
    }
}
