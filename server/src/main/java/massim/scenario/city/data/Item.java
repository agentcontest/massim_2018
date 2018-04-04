package massim.scenario.city.data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An item in the City scenario.
 */
public class Item implements Comparable<Item>{
    private String id;
    private int volume;
    private Set<Item> requiredItems;
    private Map<Item, Integer> requiredBaseItems;
    private Set<Role> rolesNeeded;
    private int value;

    public Item(String id, int volume, Set<Item> parts, Set<Role> roles){
        this.id = id;
        this.volume = volume;
        this.requiredItems = parts;
        this.rolesNeeded = roles;
        if(parts.isEmpty()) value = 0;
        else {
            value = getRequiredBaseItems().values().stream().mapToInt(i -> i).sum();
        }
    }

    /**
     * @return the item's volume
     */
    public int getVolume(){ return volume; }

    /**
     * @return the item's name
     */
    public String getName(){ return id; }

    /**
     * @return original set of required items
     */
    public Set<Item> getRequiredItems(){
        return requiredItems;
    }

    /**
     * @return a new set containing all roles needed to build this item
     */
    public Set<Role> getRequiredRoles(){
        return new HashSet<>(rolesNeeded);
    }

    /**
     * @return true, if the item needs to be assembled (with other items and/or tools)
     */
    public boolean needsAssembly(){
        return requiredItems.size() > 0 || rolesNeeded.size() > 0;
    }

    /**
     * @return the item's value
     */
    public int getValue(){ return value; }

    /**
     * @return all base items that are needed to build the item and its required items
     */
    public Map<Item, Integer> getRequiredBaseItems(){
        if(requiredBaseItems == null) {
            requiredBaseItems = new HashMap<>();
            if (!needsAssembly()) {
                requiredBaseItems.put(this, 1);
            } else {
                for (Item requiredItem : requiredItems) {
                    requiredItem.getRequiredBaseItems().forEach((item, number) -> {
                        requiredBaseItems.merge(item, number, Integer::sum);
                    });
                }
            }
        }
        return requiredBaseItems;
    }

    @Override
    public String toString(){
        String ret = "Item " + id + ": \tvol("+volume+")\tval(" + getValue() + ")";
        if(requiredItems.size() > 0)
            ret += "\tparts([" + requiredItems.stream()
                .map(Item::getName)
                .collect(Collectors.joining(", ")) + "])";
        if(rolesNeeded.size() > 0)
            ret += "\troles([" + rolesNeeded.stream().map(Role::getName).collect(Collectors.joining(", ")) + "])";
        ret += "\treqBaseIt([" + getRequiredBaseItems().entrySet().stream()
                .map(e -> "(" + e.getKey().getName() + "," + e.getValue() + ")")
                .collect(Collectors.joining(", ")) + "])";
        return ret;
    }

    @Override
    public int compareTo(Item o) {
        return this.getName().compareTo(o.getName());
    }
}
