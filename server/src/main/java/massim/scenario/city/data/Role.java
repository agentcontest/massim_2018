package massim.scenario.city.data;

import massim.protocol.scenario.city.data.RoleData;

import java.util.HashSet;
import java.util.Set;

/**
 * An agent's role in the City scenario.
 */
public class Role {

    /**
     * Data object of the role, contains serializable information.
     */
    private RoleData roleData;
    private Set<String> permissions;

    /**
     * Creates a new role.
     * @param roleData the role specifics
     * @param permissions the roads usable by this role (e.g. "air", "road", ...)
     */
    Role(RoleData roleData, Set<String> permissions){
        this.permissions = permissions;
        this.roleData = roleData;
    }

    public String getName(){
        return roleData.getName();
    }

    public int getBaseBattery(){
        return roleData.getBaseBattery();
    }

    public int getBaseLoad(){
        return roleData.getBaseLoad();
    }

    public int getBaseSpeed(){
        return roleData.getBaseSpeed();
    }

    public int getMaxBattery(){
        return roleData.getMaxBattery();
    }

    public int getMaxLoad(){
        return roleData.getMaxLoad();
    }

    public int getMaxSpeed(){
        return roleData.getMaxSpeed();
    }

    public int getBaseSkill(){
        return roleData.getBaseSkill();
    }

    public int getMaxSkill(){
        return roleData.getMaxSkill();
    }

    public int getBaseVision(){
        return roleData.getBaseVision();
    }

    public int getMaxVision(){
        return roleData.getMaxVision();
    }

    /**
     * @return the names of the usable roads for this role
     */
    public Set<String> getPermissions(){
        return new HashSet<>(permissions);
    }

    /**
     * @return the role data object for this role as used by the binding framework
     */
    public RoleData getRoleData(){
        return roleData;
    }
}
