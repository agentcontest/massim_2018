package massim.config;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Holds all massim.config values relevant for the server part. Initialized with the massim.config JSON object.
 * Also references all other massim.config objects.
 * @author ta10
 */
public class ServerConfig {
    public String tournamentMode;
    public String launch;
    public int teamSize;
    public int teamsPerMatch;
    public List<TeamConfig> teams = new Vector<>();
    public List<JSONObject> simConfigs = new Vector<>();
    public int port;
    public int backlog;
    public Map<String, String> accounts = new HashMap<>();
}