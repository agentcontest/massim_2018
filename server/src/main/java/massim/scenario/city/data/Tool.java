package massim.scenario.city.data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A tool in the City scenario.
 * Special kind of item that knows which roles can use it.
 */
public class Tool extends Item{

    private Set<String> roles = new HashSet<>();

    public Tool(String id, int volume, int value, String ... roles){
        super(id, volume, value, new HashSet<>());
        this.roles.addAll(Arrays.asList(roles));
    }

    /**
     * @return a new set containing all roles that can use this tool
     */
    public Set<String> getRoles(){
        return new HashSet<>(roles);
    }
}
