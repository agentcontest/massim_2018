package massim.scenario.city.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A product/item in the City scenario.
 */
public class Item {
    private String id;
    private int volume;
    private Map<Item, Integer> requiredItems = new HashMap<>();
    private Set<Tool> toolsNeeded = new HashSet<>();
    private int value;
    private int assembleValue;
    private Map<Item, Integer> requiredBaseItems = new HashMap<>();

    public Item(String id, int volume, int value, Set<Tool> tools){
        this.id = id;
        this.volume = volume;
        toolsNeeded.addAll(tools);
        this.value = value;
        this.assembleValue = 0;
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
     * adds a new item requirement (for crafting)
     * @param item the type required
     * @param amount the required amount
     */
    public void addRequirement(Item item, int amount){
        requiredItems.put(item, amount);
    }

    public void addRequiredTool(Tool tool){
        toolsNeeded.add(tool);
    }

    /**
     * @return mapping from products to required amounts (original map for now)
     */
    public Map<Item, Integer> getRequiredItems(){
        return requiredItems;
    }

    /**
     * @return a new set containing all tools needed to build this item
     */
    public Set<Tool> getRequiredTools(){
        return new HashSet<>(toolsNeeded);
    }

    /**
     * @return true, if the item needs to be assembled (with other items and/or tools)
     */
    public boolean needsAssembly(){
        return requiredItems.keySet().size() > 0 || toolsNeeded.size() > 0;
    }

    /**
     * @return the item's value
     */
    public int getValue(){ return value; }

    /**
     * @return the item's assembleValue
     */
    public int getAssembleValue(){
        if(assembleValue==0){
            if(needsAssembly()){
                int aValue = 1;
                for(Item item: requiredItems.keySet()){
                    aValue = aValue + requiredItems.get(item) * (item.getAssembleValue());
                }
                this.assembleValue = aValue;
                return aValue;
            }
            return 0;
        }
        return assembleValue;
    }

    /**
     * @return all base items that are needed to build the item and its required items
     */
    public Map<Item, Integer> getRequiredBaseItems(){
        if(requiredBaseItems.isEmpty()){
            if(needsAssembly()){
                Map<Item, Integer> reqBaseItems = new HashMap<>();
                for(Item item: requiredItems.keySet()){
                    if(item.needsAssembly()){
                        Map<Item, Integer> tmpItems = item.getRequiredBaseItems();
                        for(int i=0; i<requiredItems.get(item); i++){
                            for(Item tmpItem: tmpItems.keySet()){
                                if(reqBaseItems.containsKey(tmpItem)){
                                    int amount = reqBaseItems.get(tmpItem);
                                    reqBaseItems.replace(tmpItem, amount, amount + tmpItems.get(tmpItem));
                                }else{
                                    reqBaseItems.put(tmpItem,tmpItems.get(tmpItem));
                                }
                            }
                        }
                    }else{
                        if(reqBaseItems.containsKey(item)){
                            int amount = reqBaseItems.get(item);
                            reqBaseItems.replace(item, amount, amount + requiredItems.get(item));
                        }else {
                            reqBaseItems.put(item, requiredItems.get(item));
                        }
                    }
                }
                this.requiredBaseItems = reqBaseItems;
                return reqBaseItems;
            }
            return requiredBaseItems;
        }
        return requiredBaseItems;
    }

    @Override
    public String toString(){
        String ret = "Item " + id + ": \tvol("+volume+")";
        if(requiredItems.keySet().size() > 0)
            ret += "\tparts([" + requiredItems.entrySet().stream()
                .map(e -> "(" + e.getValue() + ", " + e.getKey().getName() + ")")
                .collect(Collectors.joining(", ")) + "])";
        if(toolsNeeded.size() > 0)
            ret += "\ttools([" + toolsNeeded.stream().map(Tool::getName).collect(Collectors.joining(", ")) + "])";

        return ret;
    }
}
