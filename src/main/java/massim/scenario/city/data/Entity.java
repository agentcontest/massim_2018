package massim.scenario.city.data;

import massim.messages.ActionContent;
import massim.scenario.city.ActionExecutor;

import java.util.HashSet;
import java.util.Set;

/**
 * The body of an agent in the City scenario.
 */
public class Entity {

    private Role role;
    private Location location;
    private Route route;
    private Set<Tool> acquiredTools = new HashSet<>();

    private int currentBattery;
    private BoundedItemBox items;

    private ActionContent lastAction = ActionContent.STD_NO_ACTION;
    private String lastActionResult = ActionExecutor.SUCCESSFUL;

    public Entity(Role role, Location location){
        this.role = role;
        items = new BoundedItemBox(role.getMaxLoad());
        this.location = location;
        currentBattery = role.getMaxBattery();
    }

    public int getCurrentBattery(){
        return currentBattery;
    }

    public int getCurrentLoad(){
        return items.getCurrentVolume();
    }

    public Role getRole(){
        return role;
    }

    public Location getLocation(){
        return location;
    }

    public void setLastActionResult(String lastActionResult){
        this.lastActionResult = lastActionResult;
    }

    /**
     * @return the current route or null if no route is followed
     */
    public Route getRoute(){
        return route;
    }

    /**
     * @param route the route this entity should follow now
     */
    public void setRoute(Route route){
        this.route = route;
    }

    /**
     * Moves this entity along the route.
     * @return true if successful
     */
    public boolean advanceRoute() {
        if (route == null) {
            return false;
        }
        if (currentBattery <= 5){
            route = null;
            currentBattery = 0;
            return false;
        }
        currentBattery -= 10;  // TODO 2017:: define battery cost of moving! (role depending? time-based or distance based?)
        Location newLoc = this.route.advance(role.getSpeed());
        if (newLoc != null) location = newLoc;
        if (route.isCompleted()) route = null;
        return true;
    }

    public void setLastAction(ActionContent action) {
        lastAction = action;
    }

    /**
     * @param item the item type to check
     * @return the number of items of that type the entity has stashed away
     */
    public int getItemCount(Item item){
        return items.getItemCount(item);
    }

    /**
     * @return the free volume of this entity
     */
    public int getFreeSpace() {
        return items.getFreeSpace();
    }

    /**
     * Transfers items from this entity to another. Without regard for capacity problems.
     * Only transfers items if available.
     * @param receiverEntity the entity to receive the item
     * @param item item type to transfer
     * @param amount how many items to transfer
     * @return the number of items that were actually transferred
     */
    public int transferItems(Entity receiverEntity, Item item, int amount) {
        int removed = removeItem(item, amount);
        receiverEntity.addItem(item, removed);
        return removed;
    }


    /**
     * Removes a number of items from this entity, but not more than available.
     * @param item type to remove
     * @param remove number of items to remove
     * @return the number of items that were actually removed
     */
    public int removeItem(Item item, int remove){
        return items.remove(item, remove);
    }

    /**
     * Adds a number of items to the entity if capacity allows.
     * @param item the item's type
     * @param amount number of items to add
     * @return whether the items have been added
     */
    public boolean addItem(Item item, int amount){
        return items.store(item, amount);
    }

    /**
     * @return the result of the last action
     */
    public String getLastActionResult() {
        return lastActionResult;
    }

    /**
     * @return the last action executed for this agent
     */
    public ActionContent getLastAction() {
        return lastAction;
    }

    /**
     * Charges the battery of this entity
     * @param rate the amount to charge
     */
    public void charge(int rate) {
        currentBattery = Math.min(currentBattery + rate, role.getMaxBattery());
    }

    /**
     * Just removes the current route of the entity
     */
    public void clearRoute() {
        role = null;
    }

    /**
     * @return the mutable inventory of this agent. Proceed with caution.
     */
    public ItemBox getInventory(){
        return items;
    }
}
