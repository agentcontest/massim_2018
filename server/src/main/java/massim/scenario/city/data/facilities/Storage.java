package massim.scenario.city.data.facilities;

import massim.protocol.scenario.city.data.StorageData;
import massim.protocol.scenario.city.data.StoredData;
import massim.scenario.city.data.Item;
import massim.scenario.city.data.ItemBox;
import massim.scenario.city.data.Location;

import java.util.*;

/**
 * A storage facility in the City scenario.
 */
public class Storage extends Facility{

    private int capacity;
    private int storedVolume = 0;

    /**
     * Mapping team name to a map from items to item amounts.
     */
    private Map<String, Map<Item, Integer>> storedItems = new HashMap<>();
    private Map<String, ItemBox> deliveredItems = new HashMap<>();

    public Storage(String name, Location location, int capacity, Set<String> teamNames) {
        super(name, location);
        this.capacity = capacity;
        teamNames.forEach(t -> {
            storedItems.put(t, new HashMap<>());
            deliveredItems.put(t, new ItemBox());
        });
    }

    /**
     * Stores a number of items if possible.
     * @param item a valid item
     * @param amount a positive integer - number of items to store
     * @param team name of the team to store the item for
     * @return true if the item could be stored, false if a parameter was invalid or the storage was already too full
     */
    public boolean store(Item item, int amount, String team){
        if(item == null || amount < 0 || team == null) return false;
        Map<Item, Integer> teamItems = storedItems.get(team);
        Integer storedAmount = teamItems.get(item);
        if(storedAmount == null) storedAmount = 0;
        int newVolume = storedVolume + (amount * item.getVolume());
        if (newVolume > capacity) return false;
        teamItems.put(item, storedAmount + amount);
        storedVolume = newVolume;
        return true;
    }

    /**
     * @return the free space in this storage
     */
    public int getFreeSpace(){
        return capacity - storedVolume;
    }

    /**
     * Yields the amount of an item for the given team in this storage.
     * @param item the item's type
     * @param team a team name
     * @return the amount of an item for the given team.
     */
    public int getStored(Item item, String team) {
        Map<Item, Integer> map = storedItems.get(team);
        if (item == null || map == null || team == null) return 0;
        Integer amount = map.get(item);
        return amount == null? 0: amount;
    }

    /**
     * Yields the amount of an item for the given team in this storage's "delivered" compartment.
     * @param item the item's type
     * @param team a team name
     * @return the amount of an item for the given team.
     */
    public int getDelivered(Item item, String team){
        ItemBox box = deliveredItems.get(team);
        return box == null? 0: box.getItemCount(item);
    }

    /**
     * Removes a number of "stored" items from this storage (but not more than available).
     * @param item an item type
     * @param amount amount to remove
     * @param team name of the team
     */
    public void removeStored(Item item, int amount, String team) {
        int current = getStored(item, team);
        Map<Item, Integer> map = storedItems.get(team);
        if(map != null){
            int newAmount = Math.max(current - amount, 0);
            map.put(item, newAmount);
            storedVolume -= item.getVolume() * (current - newAmount);
        }
    }

    /**
     * Removes a number of "delivered" items from this storage (but not more than available).
     * @param item an item type
     * @param amount amount to remove
     * @param team name of the team
     */
    public void removeDelivered(Item item, int amount, String team){
        ItemBox box = deliveredItems.get(team);
        if(box != null) box.remove(item, amount);
    }

    /**
     * Transfers a box of items to a team's "delivered" box.
     * @param box a box of items
     * @param team name of the team
     */
    public void addDelivered(ItemBox box, String team) {
        ItemBox targetBox = deliveredItems.get(team);
        if(targetBox != null) targetBox.addAll(box);
    }

    /**
     * @return this storage's capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Builds a snapshot of this storage.
     * @param teams the teams to include the stored item data of; note that the format for a single team differs from
     *              that of multiple teams
     * @return an object capturing the current state of the storage
     */
    public StorageData toStorageData(List<String> teams){

        List<StoredData> stored = null;
        List<StorageData.TeamStoredData> teamStored = null;

        if(teams.size() > 1){
            teamStored = new Vector<>();
            for(String team: teams){
                List<StoredData> tempStored = getStoredForTeam(team);
                if(tempStored.size() > 0) teamStored.add(new StorageData.TeamStoredData(team, tempStored));
            }
        }
        else if (teams.size() == 1){
            stored = getStoredForTeam(teams.get(0));
        }

        return new StorageData(getName(), getLocation().getLat(), getLocation().getLon(), capacity, getFreeSpace(),
                stored, teamStored);
    }

    /**
     * Builds a snapshot of what this storage currently stores for a given team.
     * @param team the team to check delivered/stored items for
     * @return the stored and delivered items for one team
     */
    private List<StoredData> getStoredForTeam(String team){
        List<StoredData> stored = new Vector<>();
        ItemBox dBox = deliveredItems.get(team);
        Map<Item, Integer> sBox = storedItems.get(team);
        Set<Item> items = new HashSet<>();
        if(dBox != null) items.addAll(dBox.getStoredTypes());
        if(sBox != null) items.addAll(sBox.keySet());

        if(items.size() > 0) {
            for (Item item : items) {
                // add an entry if item is either stored or delivered for the team
                int storedAmount = getStored(item, team);
                int deliveredAmount = getDelivered(item, team);
                if (storedAmount > 0 || deliveredAmount > 0)
                    stored.add(new StoredData(item.getName(), storedAmount, deliveredAmount));
            }
        }
        return stored;
    }

    @Override
    public String toString(){
        return super.toString() + " capacity(" + capacity + ")";
    }
}