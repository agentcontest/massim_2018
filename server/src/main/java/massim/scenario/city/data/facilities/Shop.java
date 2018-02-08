package massim.scenario.city.data.facilities;

import massim.scenario.city.data.Location;

/**
 * Shop facility in the City scenario 2018.
 */
public class Shop extends Facility{

    private int tradeModifier;

    /**
     * Creates a new shop.
     * @param name the name of the shop
     * @param location the shop's location
     * @param tradeModifier the multiplier for trade-in values
     */
    public Shop(String name, Location location, int tradeModifier) {
        super(name, location);
        this.tradeModifier = tradeModifier;
    }

    public int getTradeModifier() {
        return tradeModifier;
    }

    @Override
    public String toString(){
        return super.toString() + " tradeMod(" + tradeModifier + ")";
    }
}
