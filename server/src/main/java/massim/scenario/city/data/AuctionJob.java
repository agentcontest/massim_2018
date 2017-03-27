package massim.scenario.city.data;

import massim.protocol.scenario.city.data.AuctionJobData;
import massim.protocol.scenario.city.data.JobData;
import massim.scenario.city.data.facilities.Storage;

/**
 * An auctioned job in the City scenario.
 */
public class AuctionJob extends Job{

    private TeamState assignedTeam;

    private Integer lowestBid;
    private TeamState currentAuctionWinner;

    private int auctionTime;

    private int fine;

    /**
     * Constructor.
     * @param reward the maximum reward i.e. the maximum value a team is allowed to bid
     * @param storage the storage the items should be delivered to
     * @param begin the time the auctioning starts
     * @param end the latest step in which items can be delivered
     * @param auctionTime the number of steps to run the auction, i.e. begin at step 7 and auctionTime 5 means:
     * @param fine the amount of money the assigned team has to pay if it does not finish the job in time
     */
    public AuctionJob(int reward, Storage storage, int begin, int end, int auctionTime, int fine) {
        super(reward, storage, begin, end, JobData.POSTER_SYSTEM); // auctions can only be created by the system
        this.auctionTime = auctionTime;
        this.fine = fine;
    }

    /**
     * Assigns this job to the current lowest bidding team.
     * Sets job status accordingly.
     */
    public void assign(){
        if (lowestBid != null){
            assignedTeam = currentAuctionWinner;
            status = JobStatus.ACTIVE;
        }
        else{
            status = JobStatus.ENDED;
        }
    }

    /**
     * @return the amount of steps the auction phase should take
     */
    public int getAuctionTime(){
        return auctionTime;
    }

    /**
     * @return the fine to pay if the job is not completed
     */
    public int getFine(){
        return fine;
    }

    /**
     * @return the current lowest bid (may be null)
     */
    public Integer getLowestBid(){
        return lowestBid;
    }

    /**
     * An auction job is set to {@link massim.scenario.city.data.Job.JobStatus#AUCTION in its begin step.}
     * Needs to be called in the job's begin step.
     */
    @Override
    public void activate(){
        status = JobStatus.AUCTION;
    }

    /**
     * @return true if the job has been assigned to a team
     */
    public boolean isAssigned() {
        return assignedTeam != null;
    }

    /**
     * @return the team this job was assigned to or an empty string if it has not been assigned
     */
    public String getAuctionWinner() {
        return assignedTeam != null? assignedTeam.getName(): "";
    }

    /**
     * Bids a certain reward for this job.
     * If the bid exceeds the max reward, it is silently ignored.
     * @param team the team that is bidding
     * @param amount the amount to bid
     */
    public void bid(TeamState team, int amount){
        if(amount > getReward()) return;
        if (lowestBid == null || amount < lowestBid){
            lowestBid = amount;
            currentAuctionWinner = team;
        }
    }

    @Override
    public void terminate(){
        if(!(status == JobStatus.COMPLETED) && assignedTeam != null){
            assignedTeam.subMoney(fine);
        }
        super.terminate();
    }

    @Override
    public JobData toJobData(boolean withDelivered, boolean withPoster){
        return new AuctionJobData(getName(), getStorage().getName(), getBeginStep(), getEndStep(), getReward(),
                getRequiredItems().toItemAmountData(),
                fine, lowestBid, auctionTime,
                withDelivered? getDeliveredData() : null,
                withPoster? getPoster() : null);
    }
}
