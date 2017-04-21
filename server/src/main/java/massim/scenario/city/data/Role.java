package massim.scenario.city.data;

import massim.protocol.scenario.city.data.RoleData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * An agent's role in the City scenario.
 */
public class Role {

    /**
     * Data object of the role, contains serializable information.
     */
    private RoleData roleData;
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
        this.permissions = permissions;
        roleData = new RoleData(name, speed, battery, load, new Vector<>());
    }

    /**
     * @return the name of this role
     */
    public String getName(){
        return roleData.getName();
    }

    /**
     * @return the maximum battery charge
     */
    public int getMaxBattery(){
        return roleData.getBattery();
    }

    /**
     * @return the maximum volume this role may load
     */
    public int getMaxLoad(){
        return roleData.getLoad();
    }

    /**
     * @return the speed of this role
     */
    public int getSpeed(){
        return roleData.getSpeed();
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

    /**
     * Adds a collection of tools to this role.
     * @param tools the tools to add
     */
    public void addTools(Collection<Tool> tools){
        this.tools.addAll(tools);
        tools.forEach(tool -> roleData.addTool(tool.getName()));
    }

    /**
     * @return the role data object for this role as used by the binding framework
     */
    public RoleData getRoleData(){
        return roleData;
    }
}
