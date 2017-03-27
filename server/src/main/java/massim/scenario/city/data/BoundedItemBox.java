package massim.scenario.city.data;

/**
 * A box to store a limited volume of items in.
 */
public class BoundedItemBox extends ItemBox{

    private int capacity;
    private int currentVolume = 0;

    BoundedItemBox(int capacity){
        this.capacity = capacity;
    }

    @Override
    public boolean store(Item item, int amount){
        int volumeToStore = item.getVolume() * amount;
        if(volumeToStore <= getFreeSpace() && super.store(item, amount)){
            currentVolume += volumeToStore;
            return true;
        }
        return false;
    }

    @Override
    public int remove(Item item, int amount){
        int removed = super.remove(item, amount);
        currentVolume -= item.getVolume() * removed;
        return removed;
    }

    @Override
    public boolean removeIfPossible(Item item, int amount){
        boolean removed = super.removeIfPossible(item, amount);
        if(removed) currentVolume -= item.getVolume() * amount;
        return removed;
    }

    /**
     * @return the free volume in this box
     */
    int getFreeSpace(){
        return capacity - currentVolume;
    }

    /**
     * @return the total volume of items in this box
     */
    int getCurrentVolume(){
        return currentVolume;
    }

    /**
     * @return the capacity of this box
     */
    int getCapacity(){
        return capacity;
    }
}
