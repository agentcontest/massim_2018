package massim.scenario.city.data;

import massim.scenario.city.data.facilities.Storage;

// TODO: add auction jobs to percepts
/**
 * An auctioned job in the City scenario.
 */
public class AuctionJob extends Job{

    private String assignedTeam;

    private Integer lowestBid;
    private String currentAuctionWinner;

    private int auctionTime;

    /**
     * Constructor.
     * @param reward the maximum reward i.e. the maximum value a team is allowed to bid
     * @param storage the storage the items should be delivered to
     * @param begin the time the auctioning starts
     * @param end the latest step in which items can be delivered
     * @param auctionTime the number of steps to run the auction, i.e. begin at step 7 and auctionTime 5 means:
     */
    public AuctionJob(int reward, Storage storage, int begin, int end, int auctionTime) {
        super(reward, storage, begin, end);
        this.auctionTime = auctionTime;
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
     * @return the team this job was assigned to or null if it has not been assigned
     */
    public String getAuctionWinner() {
        return assignedTeam;
    }

    /**
     * @return the currently lowest bid for this job
     */
    public int getLowestBid(){
        return lowestBid;
    }

    /**
     * Bids a certain reward for this job.
     * @param team the team that is bidding
     * @param amount the amount to bid
     */
    public void bid(String team, int amount){
        if (lowestBid == null || amount < lowestBid){
            lowestBid = amount;
            currentAuctionWinner = team;
        }
    }

    /**
     * @return the team that currently holds the lowest bid or null, if no team has posted a valid bid yet
     */
    public String getCurrentAuctionWinner(){
        return currentAuctionWinner;
    }

    @Override
    public void terminate(){
       if(!(status == JobStatus.COMPLETED)){
           // TODO subtract fine (but not here)
       }
    }
}
