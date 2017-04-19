package massim.scenario.city.data;


import massim.protocol.scenario.city.data.JobData;
import massim.protocol.scenario.city.data.MissionData;
import massim.scenario.city.data.facilities.Storage;

/**
 * A mission is a special type of job that has to be completed by all teams.
 */
public class Mission extends AuctionJob {

    private String missionID;

    /**
     * Constructor.
     *
     * @param reward      the maximum reward i.e. the maximum value a team is allowed to bid
     * @param storage     the storage the items should be delivered to
     * @param begin       the time the auctioning starts
     * @param end         the latest step in which items can be delivered
     * @param fine        the amount of money the assigned team has to pay if it does not finish the job in time
     * @param team        the team that this mission will be assigned to
     * @param missionID   the ID for the mission (the job name will differ per team)
     */
    public Mission(int reward, Storage storage, int begin, int end, int fine, TeamState team, String missionID) {
        super(reward + 1, storage, begin, end, 0, fine); // create an auction with no auctioning phase
        this.missionID = missionID;
        bid(team, reward); // place the fixed bid
        assign(); // immediately assign
    }

    @Override
    public void activate(){
        status = JobStatus.ACTIVE;
    }

    /**
     * @return the ID of this mission which is the same for all jobs derived from the same mission
     */
    public String getMissionID(){
        return missionID;
    }

    @Override
    public JobData toJobData(boolean withDelivered, boolean withPoster){
        return new MissionData(getName(), getStorage().getName(), getBeginStep(), getEndStep(), getReward(),
                getRequiredItems().toItemAmountData(),
                getFine(), getLowestBid(), getAuctionTime(),
                withDelivered? getDeliveredData() : null,
                withPoster? getPoster() : null,
                withDelivered? missionID : null);
    }
}
