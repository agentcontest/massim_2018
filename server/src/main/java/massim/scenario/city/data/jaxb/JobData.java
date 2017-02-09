package massim.scenario.city.data.jaxb;

import massim.scenario.city.data.Job;

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
    private JobData() {}

    /**
     * Constructor.
     * @param job the job to store the data of
     */
    public JobData(Job job) {
        id = job.getName();
        storage = job.getStorage().getName();
        end = job.getEndStep();
        reward = job.getReward();
        job.getRequiredItems().entrySet().forEach(entry ->
                requiredItems.add(new ItemAmountData(entry.getKey().getName(), entry.getValue())));
    }

    /**
     * @return the job ID
     */
    private String getId(){
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
}
