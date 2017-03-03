package massim.scenario.city.data;

import massim.protocol.scenario.city.data.ItemAmountData;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Container for items.
 */
public class ItemBox {

    private Map<Item, Integer> items = new HashMap<>();

    /**
     * Stores a number of items in this box.
     * @param item an item type
     * @param amount amount to store
     * @return whether the items could be stored
     */
    public boolean store(Item item, int amount){
        int stored = getItemCount(item);
        items.put(item, stored + amount);
        return true;
    }

    /**
     * Gets stored number of an item.
     * @param item an item type
     * @return the number of items stored in this box
     */
    public int getItemCount(Item item){
        Integer stored = items.get(item);
        return stored == null? 0: stored;
    }

    /**
     * Removes as many items as possible up to a given amount.
     * @param item an item type
     * @param amount maximum amount to remove
     * @return how many items could be removed
     */
    public int remove(Item item, int amount){
        int stored = getItemCount(item);
        int remove = Math.min(amount, stored);
        items.put(item, stored - remove);
        return remove;
    }

    /**
     * Removes a number of items if enough items are available (or none).
     * @param item an item type
     * @param amount how many items to remove
     * @return whether the items were removed or not
     */
    public boolean removeIfPossible(Item item, int amount){
        int stored = getItemCount(item);
        if (amount > stored) return false;
        items.put(item, stored - amount);
        return true;
    }

    /**
     * Adds (copies) all items from another box to this box. Items in the source box are not deleted.
     * @param box the box to take items from
     */
    public void addAll(ItemBox box) {
        box.getStoredTypes().forEach(item -> store(item, box.getItemCount(item)));
    }

    /**
     * @return a new set containing all item types currently stored in this box
     */
    public Set<Item> getStoredTypes(){
        return new HashSet<>(items.keySet());
    }

    /**
     * @return a list of all items with their stored quantities (if that amount is > 0)
     */
    public List<ItemAmountData> toItemAmountData(){
        return items.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(e -> new ItemAmountData(e.getKey().getName(), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Applys the given consumer to all pairs of items with their quantities in this box.
     * @param consumer the consumer to process the data
     */
    public void forEach(BiConsumer<Item, Integer> consumer){
        items.forEach(consumer);
    }

    /**
     * @param compareBox another box
     * @return true, if all item quantities in this box are at least present in a given other box
     */
    public boolean isSubset(ItemBox compareBox){
        for(Item item: getStoredTypes()){
            if(compareBox.getItemCount(item) < getItemCount(item)) return false;
        }
        return true;
    }
}
