package massim.scenario.city.data.jaxb;

import massim.scenario.city.data.Job;
import massim.scenario.city.percept.CityInitialPercept;

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

    @XmlElement(name = "items")
    private List<CityInitialPercept.ItemAmountData> requiredItems = new Vector<>();

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
        job.getRequiredItems().entrySet().forEach(entry ->
                requiredItems.add(new CityInitialPercept.ItemAmountData(entry.getKey().getName(), entry.getValue())));
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
}
