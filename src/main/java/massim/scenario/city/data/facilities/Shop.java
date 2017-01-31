package massim.scenario.city.data.facilities;

import massim.scenario.city.data.Item;
import massim.scenario.city.data.ItemBox;
import massim.scenario.city.data.Location;

import java.util.HashMap;
import java.util.Map;

/**
 * Shop facility in the City scenario.
 */
public class Shop extends Facility{

    private ItemBox stock = new ItemBox();
    private Map<Item, Integer> prices = new HashMap<>();

    public Shop(String name, Location location) {
        super(name, location);
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
}
