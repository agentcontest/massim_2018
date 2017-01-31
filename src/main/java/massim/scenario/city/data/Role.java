package massim.scenario.city.data;

import java.util.HashSet;
import java.util.Set;

/**
 * An agent's role in the City scenario.
 */
public class Role {

    private String name;
    private int battery;
    private int load;
    private int speed;
    private Set<String> permissions;
    private Set<Tool> tools = new HashSet<>();

    /**
     * Creates a new role.
     * @param name the role's name
     * @param battery the maximum charge
     * @param load the maximum load capacity
     * @param permissions the roads usable by this role (e.g. "air", "road", ...)
     */
    public Role(String name, int speed, int battery, int load, Set<String> permissions){
        this.name = name;
        this.battery = battery;
        this.load = load;
        this.speed = speed;
        this.permissions = permissions;
    }

    /**
     * @return the name of this role
     */
    public String getName(){
        return name;
    }

    /**
     * @return the maximum battery charge
     */
    public int getMaxBattery(){
        return battery;
    }

    /**
     * @return the maximum volume this role may load
     */
    public int getMaxLoad(){
        return load;
    }

    /**
     * @return the speed of this role
     */
    public int getSpeed(){
        return speed;
    }

    /**
     * @return the names of the usable roads for this role
     */
    public Set<String> getPermissions(){
        return new HashSet<>(permissions);
    }

    /**
     * @return a new set containing all tools usable by this role
     */
    public Set<Tool> getTools(){
        return new HashSet<>(tools);
    }
}
