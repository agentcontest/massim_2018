package eismassim;

import eis.EIDefaultImpl;
import eis.exceptions.*;
import eis.iilang.Action;
import eis.iilang.EnvironmentState;
import eis.iilang.IILElement;
import eis.iilang.Percept;
import massim.Log;
import massim.scenario.city.ActionExecutor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;

/**
 * Environment interface to the MASSim server following the Environment Interface Standard (EIS).
 * Supports the City Scenario 2017.
 */
public class EnvironmentInterface extends EIDefaultImpl{

    private Set<String> supportedActions = new HashSet<>();
    private Map<String, EISEntity> entities = new HashMap<>();

    /**
     * Constructor.
     */
    public EnvironmentInterface() {
        super();
        supportedActions.addAll(ActionExecutor.ALL_ACTIONS);
        try {
            parseConfig();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        entities.values().forEach(entity -> {
            try {
                addEntity(entity.getName());
            } catch (EntityException e) {
                e.printStackTrace();
            }
        });
        IILElement.toProlog = true;
        try {
            setState(EnvironmentState.PAUSED);
        } catch (ManagementException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    protected LinkedList<Percept> getAllPerceptsFromEntity(String name) throws PerceiveException, NoEnvironmentException {
        EISEntity e = entities.get(name);
        if (e == null) throw new PerceiveException("unknown entity");
        if (!e.isConnected()) throw new PerceiveException("no valid connection");
        return e.getAllPercepts();
    }

    @Override
    protected boolean isSupportedByEnvironment(Action action) {
        return action != null && supportedActions.contains( action.getName());
    }

    @Override
    protected boolean isSupportedByType(Action action, String type) {
        return action != null && supportedActions.contains(action.getName());
    }

    @Override
    protected boolean isSupportedByEntity(Action action, String entity) {
        return action != null && supportedActions.contains(action.getName());
    }

    @Override
    protected Percept performEntityAction(String name, Action action) throws ActException {
        EISEntity entity = entities.get(name);
        entity.performAction(action);
        return new Percept("done");
    }

    /**
     * Parses the eismassimconfig.json file.
     * In case of invalid configuration, standard values are used where reasonable.
     * @throws ParseException in case configuration is invalid and no reasonable standard value can be assumed
     */
    private void parseConfig() throws ParseException {

        JSONObject config = new JSONObject();
        try {
            config = new JSONObject(new String(Files.readAllBytes(Paths.get("eismassimconfig.json"))));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // parse host, port and scenario
        String host = config.optString("host", "localhost");
        int port = config.optInt("port", 12300);
        String scenario = config.optString("scenario", "city2017");
        Log.log(Log.NORMAL, "Configuring EIS: " + scenario + "@" + host + ":" + port);

        // annotate percepts with timestamps
        if(config.optBoolean("times", true)){
            EISEntity.enableTimeAnnotations();
            Log.log(Log.NORMAL, "Timed annotations enabled.");
        }

        // enable scheduling
        if(config.optBoolean("scheduling", true)){
            EISEntity.enableScheduling();
            Log.log(Log.NORMAL, "Scheduling enabled.");
        }

        // enable notifications
        if(config.optBoolean("notifications", true)){
            EISEntity.enableNotifications();
            Log.log(Log.NORMAL, "Notifications enabled.");
        }

        // timeout
        EISEntity.setTimeout(config.optInt("timeout", 3900));
        Log.log(Log.NORMAL, "Timeout set to " + EISEntity.timeout);

        // queue
        if(config.optBoolean("queued", true)){
            EISEntity.enablePerceptQueue();
            Log.log(Log.NORMAL, "Percept queue enabled.");
        }

        // parse entities
        JSONArray jsonEntities = config.optJSONArray("entities");
        if(jsonEntities == null) jsonEntities = new JSONArray();
        for (int i = 0; i < jsonEntities.length(); i++) {
            JSONObject jsonEntity = jsonEntities.optJSONObject(i);
            if(jsonEntity == null) continue;
            String name = jsonEntity.optString("name");
            if (name == null) throw new ParseException("Entity must have a valid name", 0);
            String username = jsonEntity.optString("username");
            if (username == null) throw new ParseException("Entity must have a valid username", 0);
            String password = jsonEntity.optString("password");
            if (password == null) throw new ParseException("Entity must have a valid password", 0);

            // instantiate entity
            EISEntity entity = EISEntity.createEntity(name, scenario, host, port, username, password);

            // verbose
            if(config.optBoolean("xml", true)){
                entity.enableXML();
                Log.log(Log.NORMAL, "Enable XML for entity " + entity.getName());
            }

            if(config.optBoolean("iilang", true)){
                entity.enableIILang();
                Log.log(Log.NORMAL, "Enable IILang for entity " + entity.getName());
            }

            entities.put(entity.getName(), entity);
        }
    }
}
