package massim.scenario.city.data.jaxb;

import massim.scenario.city.data.AuctionJob;

import javax.xml.bind.annotation.XmlAttribute;

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
     * @param job the job to store the data of
     */
    public AuctionJobData(AuctionJob job) {
        super(job);
        fine = job.getFine();
        lowestBid = job.getLowestBid();
        auctionTime = job.getAuctionTime();
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
