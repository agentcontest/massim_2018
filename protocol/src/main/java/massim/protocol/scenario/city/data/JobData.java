package massim.protocol.scenario.city.data;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Vector;

/**
 * Holds data of a regular job.
 */
@XmlRootElement(name = "job")
@XmlAccessorType(XmlAccessType.NONE)
public class JobData {

    public static final String POSTER_SYSTEM = "system";

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String storage;

    @XmlAttribute
    private int start;

    @XmlAttribute
    private int end;

    @XmlAttribute
    private int reward;

    @XmlAttribute
    private String poster;

    @XmlElement(name = "required")
    private List<ItemAmountData> requiredItems = new Vector<>();

    /**
     * May be null.
     */
    @XmlElement(name="delivered")
    private List<CompletionData> deliveredItems;

    /**
     * For jaxb
     */
    JobData() {}

    /**
     * Constructor.
     * @param name id of the job
     * @param storage storage to deliver to
     * @param start start step
     * @param end end step
     * @param reward reward amount
     * @param requiredItems items required for job completion
     * @param poster the job's origin
     */
    public JobData(String name, String storage, int start, int end, int reward, List<ItemAmountData> requiredItems,
                   List<CompletionData> deliveredItems, String poster) {
        id = name;
        this.storage = storage;
        this.start = start;
        this.end = end;
        this.reward = reward;
        if(requiredItems != null) this.requiredItems.addAll(requiredItems);
        this.deliveredItems = deliveredItems;
        this.poster = poster;
    }

    /**
     * @return the job ID
     */
    public String getId(){
        return id;
    }

    /**
     * @return the storage associated with this job
     */
    public String getStorage() {
        return storage == null? "" : storage;
    }

    /**
     * @return the start step of the job
     */
    public int getStart() {
        return start;
    }

    /**
     * @return the end step of the job
     */
    public int getEnd() {
        return end;
    }

    /**
     * @return the job's reward (max reward in case of auction)
     */
    public int getReward(){
        return reward;
    }

    /**
     * @return a list of items that are required to complete the job
     */
    public List<ItemAmountData> getRequiredItems(){
        return requiredItems;
    }

    /**
     * @return which team delivered which items to this job. May be null if no team delivered items yet
     * (or that data is not desired here)
     */
    public List<CompletionData> getDeliveredItems(){
        return deliveredItems;
    }

    /**
     * @return the poster of this job or an empty string if that info is not available
     */
    public String getPoster(){
        return poster == null? "" : poster;
    }

    /**
     * Stores how many items of which type a team already delivered.
     */
    @XmlRootElement(name = "delivered")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class CompletionData {

        @XmlAttribute
        public String team;

        @XmlElement(name="item")
        public List<ItemAmountData> delivered;

        /**
         * For jaxb.
         */
        private CompletionData(){}

        /**
         * Constructor.
         * @param team name of the team
         * @param deliveredItems items delivered to the job by one team
         */
        public CompletionData(String team, List<ItemAmountData> deliveredItems){
            this.team = team;
            delivered = deliveredItems;
        }
    }
}
