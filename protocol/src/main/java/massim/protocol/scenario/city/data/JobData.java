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

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String storage;

    @XmlAttribute
    private int end;

    @XmlAttribute
    private int reward;

    @XmlElement(name = "items")
    private List<ItemAmountData> requiredItems = new Vector<>();

    /**
     * For jaxb
     */
    JobData() {}

    /**
     * Constructor.
     * @param name id of the job
     * @param storage storage to deliver to
     * @param end end step
     * @param reward reward amount
     * @param requiredItems items required for job completion
     */
    public JobData(String name, String storage, int end, int reward, List<ItemAmountData> requiredItems) {
        id = name;
        this.storage = storage;
        this.end = end;
        this.reward = reward;
        if(requiredItems != null) this.requiredItems.addAll(requiredItems);
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
        return storage;
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
}
