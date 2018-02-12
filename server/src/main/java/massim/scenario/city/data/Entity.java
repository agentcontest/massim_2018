package massim.scenario.city.data;

import massim.protocol.messagecontent.Action;
import massim.scenario.city.ActionExecutor;

/**
 * The body of an agent in the City scenario.
 */
public class Entity {

    private Role role;
    private Location location;
    private Route route;

    private int skill;
    private int battery;
    private int speed;
    private int vision;

    private int currentBattery;
    private BoundedItemBox items;

    private Action lastAction = Action.STD_NO_ACTION;
    private String lastActionResult = ActionExecutor.SUCCESSFUL;

    Entity(Role role, Location location){
        this.role = role;
        skill = role.getBaseSkill();
        battery = role.getBaseBattery();
        speed = role.getBaseSpeed();
        vision = role.getBaseVision();

        items = new BoundedItemBox(role.getBaseLoad());
        this.location = location;
        currentBattery = battery;
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
     * @param cost the energy cost of the goto action
     * @return true if successful
     */
    public boolean advanceRoute(int cost) {
        if (route == null) {
            return false;
        }
        if (currentBattery < cost){
            route = null;
            return false;
        }
        currentBattery -= cost;
        Location newLoc = this.route.advance(speed);
        if (newLoc != null) location = newLoc;
        if (route.isCompleted()) route = null;
        return true;
    }

    public void setLastAction(Action action) {
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
    public Action getLastAction() {
        return lastAction;
    }

    /**
     * Charges the battery of this entity
     * @param rate the amount to charge
     */
    public void charge(int rate) {
        currentBattery = Math.min(currentBattery + rate, battery);
    }

    /**
     * Just removes the current route of the entity
     */
    public void clearRoute() {
        route = null;
    }

    /**
     * @return the mutable inventory of this agent. Proceed with caution.
     */
    public ItemBox getInventory(){
        return items;
    }

    /**
     * "Moves" this entity to a new location.
     * @param newLoc the location to move the entity to
     */
    public void setLocation(Location newLoc){
        location = newLoc;
    }

    /**
     * Removes all items from this entity.
     */
    public void clearInventory() {
        items = new BoundedItemBox(items.getCurrentVolume() + items.getFreeSpace());
    }

    /**
     * Completely drains the entity's battery.
     */
    public void discharge(){
        this.currentBattery = 0;
    }

    public int getSkill() {
        return skill;
    }

    public int getSpeed() {
        return speed;
    }

    public int getVision() {
        return vision;
    }

    public int getBatteryCapacity () {
        return battery;
    }

    public int getLoadCapacity() {
        return items.getCapacity();
    }

    public void upgrade(Upgrade upgrade) {
        switch(upgrade.getName()) {
            case "skill":
                this.skill = Math.min(skill + upgrade.getStep(), role.getMaxSkill());
                break;
            case "battery":
                this.battery = Math.min(battery + upgrade.getStep(), role.getMaxBattery());
                break;
            case "load":
                items.extend(upgrade.getStep(), role.getMaxLoad());
                break;
            case "speed":
                this.speed = Math.min(speed + upgrade.getStep(), role.getMaxSpeed());
                break;
            case "vision":
                this.vision = Math.min(vision + upgrade.getStep(), role.getMaxVision());
                break;
        }
    }
}
