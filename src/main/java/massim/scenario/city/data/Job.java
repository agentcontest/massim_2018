package massim.scenario.city.data;

import massim.scenario.city.data.facilities.Storage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A job in the City scenario.
 */
public class Job {

    private final static String SOURCE_SYSTEM = "system";
    private final static AtomicInteger counter = new AtomicInteger();

    private JobStatus status = JobStatus.FUTURE;
    private String name = "";
    private String source;
    private Storage storage;
    private int reward;

    private Map<Item, Integer> requiredItems = new HashMap<>();
    private Map<String, ItemBox> deliveredItems = new HashMap<>();

    public Job(int reward, String source, Storage storage){
        this.source = source;
    }

    public boolean isActive(){
        return status == JobStatus.ACTIVE;
    }

    public String getSource(){
        return source;
    }

    public Storage getStorage(){
        return storage;
    }

    public void addRequiredItem(Item item, int amount){
        requiredItems.put(item, amount);
    }

    public Map<Item, Integer> getRequiredItems(){
        return requiredItems;
    }

    /**
     * Delivers items towards the job, but not more than necessary.
     * @param item an item type
     * @param amount the amount to deliver
     * @param team the team to deliver for
     * @return how many items were actually delivered
     */
    public int deliver(Item item, int amount, String team) {
        if(!(status == JobStatus.ACTIVE)) return 0;
        ItemBox box = getDelivered(team);
        int missing = requiredItems.get(item) - box.getItemCount(item);
        int store = Math.min(missing, amount);
        box.store(item, store);
        return store;
    }

    /**
     * Gets the box containing all items delivered by the given team towards this job.
     * @param team name of the team
     * @return the box
     */
    public ItemBox getDelivered(String team){
        deliveredItems.putIfAbsent(team, new ItemBox());
        return deliveredItems.get(team);
    }

    /**
     * @return how much money the job awards
     */
    public int getReward(){
        return reward;
    }

    /**
     * Checks if the job has been completed.
     * Sets status accordingly.
     * Moves reward items and partial deliveries to the "delivered" storage.
     * @param team the team to check completion for
     * @return whether the job was completed by that team (and has been active up to now)
     */
    public boolean checkCompletion(String team) {
        if(status == JobStatus.ACTIVE) {
            ItemBox delivered = getDelivered(team);
            boolean completed = true;
            for (Map.Entry<Item, Integer> entry : requiredItems.entrySet()) {
                // check if all quantities suffice
                if (delivered.getItemCount(entry.getKey()) < entry.getValue()) {
                    completed = false;
                    break;
                }
            }
            if(completed){
                status = JobStatus.COMPLETED;
                // transfer items
                deliveredItems.entrySet().stream()
                        .filter(entry -> !entry.getKey().equals(team)) // completing team does not get items
                        .forEach(entry -> storage.addDelivered(entry.getValue(), entry.getKey()));
                if(!source.equals(SOURCE_SYSTEM)){ // posting team gets required items
                    requiredItems.entrySet().forEach(entry -> storage.addDelivered(entry.getKey(), entry.getValue(), source));
                }
                return true;
            }
        }
        return false;
    }

    /**
     * @return the name of this job
     */
    public String getName(){
        return name;
    }

    /**
     * Gives the job a name if it has none.
     */
    public void acquireName(){
        if(name.equals("")) name = "job" + counter.getAndIncrement();
    }

    /**
     * The possible states of a job.<br>
     * {@link #ACTIVE}: job has been started and is not cancelled/completed <br>
     * {@link #AUCTION}: job is currently up for auction <br>
     * {@link #CANCELLED}: job has been cancelled <br>
     * {@link #COMPLETED}: job has been completed by a team <br>
     * {@link #FUTURE}: job has been created and not activated yet.<br>
     */
    public enum JobStatus{
        ACTIVE, CANCELLED, COMPLETED, AUCTION, FUTURE
    }
}
