package massim.scenario.city.data.facilities;

import massim.scenario.city.data.Item;
import massim.scenario.city.data.ItemBox;
import massim.scenario.city.data.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Shop facility in the City scenario.
 */
public class Shop extends Facility{

    private ItemBox stock = new ItemBox();
    private Map<Item, Integer> prices = new HashMap<>();
    private Map<Item, Integer> initialAmounts = new HashMap<>();
    private int restock;
    private int nextRestock;

    /**
     * Creates a new shop.
     * @param name the name of the shop
     * @param location the shop's location
     * @param restock how many steps until and between the shop restocks one of each item
     */
    public Shop(String name, Location location, int restock) {
        super(name, location);
        this.restock = restock;
        this.nextRestock = restock;
    }

    /**
     * @param item an item type
     * @return how many items of a given type this shop currently stocks
     */
    public int getItemCount(Item item) {
        return stock.getItemCount(item);
    }

    /**
     * Adds an item to the shops offerings.
     * @param item an item type
     * @param initialAmount the initial amount of that item the shop is selling
     * @param price the price at which to sell
     */
    public void addItem(Item item, int initialAmount, int price){
        prices.put(item, price);
        stock.store(item, initialAmount);
        initialAmounts.put(item, initialAmount);
    }

    /**
     * @param item an item type
     * @return the price of an item or 0 if there is no price
     */
    public int getPrice(Item item){
        Integer price = prices.get(item);
        return price == null? 0: price;
    }

    /**
     * Buys (i.e. removes) a quantity of items from this shop.
     * @param item an item type
     * @param amount how many items to buy
     * @return the total price that has to be paid for the items
     */
    public int buy(Item item, int amount){
        return stock.remove(item, amount) * getPrice(item);
    }

    /**
     * Adds a quantity of an item to be sold.
     * @param item an item type
     * @param amount the amount to restock
     */
    public void restock(Item item, int amount){
        stock.store(item, amount);
    }

    /**
     * @return all items that were ever offered by this shop
     */
    public Set<Item> getOfferedItems(){
        return stock.getStoredTypes();
    }

    /**
     * Should be called after each step. Triggers restocking.
     */
    public void step(){
        nextRestock = Math.max(0, nextRestock - 1);
        if(nextRestock == 0){
            nextRestock = restock;
            //stock.getStoredTypes().forEach(item -> restock(item, 1));
            for(Item item: stock.getStoredTypes()){
                if(getItemCount(item) < initialAmounts.get(item)){
                    restock(item, 1);
                }
            }
        }
    }

    /**
     * @return the restock interval of this shop
     */
    public int getRestock() {
        return restock;
    }

    public int getInitialAmount(Item item){
        Integer amount = initialAmounts.get(item);
        return amount == null? 0: amount;
    }

    @Override
    public String toString(){
        return super.toString() + " restock(" + restock + ") \tstock([" +
                stock.getStoredTypes().stream()
                        .map(item -> "("+getItemCount(item) + ", " + item.getName() + ", " + getPrice(item) + ")")
                        .collect(Collectors.joining(", ")) + "])";
    }
}
