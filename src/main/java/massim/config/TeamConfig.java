package massim.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents one configured team.
 * @author ta10
 */
public class TeamConfig {
    private String name;
    private Map<String, String> agents = new HashMap<>();

    public TeamConfig(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void addAgent(String name, String password){
        agents.put(name, password);
    }

    /**
     * @return Mapping from agent names to passwords
     */
    public Map<String, String> getAgents(){
        return agents;
    }
}
