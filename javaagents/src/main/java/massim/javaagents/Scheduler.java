package massim.javaagents;

import eis.AgentListener;
import eis.EnvironmentInterfaceStandard;
import eis.EnvironmentListener;
import eis.exceptions.AgentException;
import eis.exceptions.PerceiveException;
import eis.exceptions.RelationException;
import eis.iilang.EnvironmentState;
import eis.iilang.Percept;
import massim.javaagents.agents.Agent;
import massim.javaagents.agents.BasicAgent;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * A scheduler for agent creation and execution.
 * EISMASSim scheduling needs to be enabled (via config), so that getAllPercepts()
 * blocks until new percepts are available!
 * (Also, queued and notifications should be disabled)
 */
public class Scheduler implements AgentListener, EnvironmentListener{

    /**
     * Holds configured agent data.
     */
    private class AgentConf {
        String name;
        String entity;
        String team;
        String className;

        AgentConf(String name, String entity, String team, String className){
            this.name = name;
            this.entity = entity;
            this.team = team;
            this.className = className;
        }
    }

    private EnvironmentInterfaceStandard eis;
    private List<AgentConf> agentConfigurations = new Vector<>();
    private Map<String, Agent> agents = new HashMap<>();

    /**
     * Create a new scheduler based on the given configuration file
     * @param path path to a java agents configuration file
     */
    Scheduler(String path) {
        parseConfig(path);
    }

    /**
     * Parses the java agents config.
     * @param path the path to the config
     */
    private void parseConfig(String path) {
        try {
            JSONObject config = new JSONObject(new String(Files.readAllBytes(Paths.get(path, "javaagentsconfig.json"))));
            JSONObject agents = config.optJSONObject("agents");
            if(agents != null){
                agents.keySet().forEach(agName -> {
                    JSONObject agConf = agents.getJSONObject(agName);
                    agentConfigurations.add(new AgentConf(agName, agConf.getString("entity"), agConf.getString("team"),
                                                          agConf.getString("class")));
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connects to an Environment Interface
     * @param ei the interface to connect to
     */
    void setEnvironment(EnvironmentInterfaceStandard ei) {
        this.eis = ei;
        MailBox mailBox = new MailBox();
        for (AgentConf agentConf: agentConfigurations) {

            Agent agent = null;
            switch(agentConf.className){
                case "BasicAgent":
                    agent = new BasicAgent(agentConf.name, mailBox);
                    break;
                // [add further types here]
                default:
                    System.out.println("Unknown agent type/class " + agentConf.className);
            }
            if(agent == null) continue;

            try {
                ei.registerAgent(agent.getName());
            } catch (AgentException e) {
                e.printStackTrace();
            }

            try {
                ei.associateEntity(agent.getName(), agentConf.entity);
            } catch (RelationException e) {
                e.printStackTrace();
            }
            System.out.println("associated agent \"" + agent.getName() + "\" with entity \"" + agentConf.entity + "\"");

            ei.attachAgentListener(agent.getName(), this);
            agents.put(agentConf.name, agent);
        }
        ei.attachEnvironmentListener(this);
    }

    /**
     * Steps all agents and relevant infrastructure.
     */
    void step() {
        agents.values().forEach(ag -> {
            List<Percept> percepts = new Vector<>();
            try {
                eis.getAllPercepts(ag.getName()).values().forEach(percepts::addAll);
            } catch (PerceiveException e) {
                e.printStackTrace();
            }
            ag.setPercepts(percepts);
        });
    }

    @Override
    public void handlePercept(String agent, Percept percept) {
        agents.get(agent).handlePercept(percept);
    }

    @Override
    public void handleStateChange(EnvironmentState newState) {}

    @Override
    public void handleFreeEntity(String entity, Collection<String> agents) {}

    @Override
    public void handleDeletedEntity(String entity, Collection<String> agents) {}

    @Override
    public void handleNewEntity(String entity) {}
}
