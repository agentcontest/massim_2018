package massim.config;

import org.json.JSONObject;

import java.util.*;

/**
 * Holds all massim.config values relevant for the server part. Initialized with the massim.config JSON object.
 * Also references all other massim.config objects.
 * @author ta10
 */
public class ServerConfig {

    public final static String MODE_ROUND_ROBIN = "round-robin";
    public final static String MODE_MANUAL = "manual";
    public final static String MODE_RANDOM = "random";

    public String tournamentMode;
    public String launch;
    public int teamSize;
    public int teamsPerMatch;
    public List<TeamConfig> teams = new Vector<>();
    public List<JSONObject> simConfigs = new Vector<>();
    public int port;
    public int backlog;
    public Map<String, String> accounts = new HashMap<>();
    public long agentTimeout;
    public String logPath;
    public String resultPath;
    public String logLevel;
    public List<Set<TeamConfig>> manualModeTeams;
    public int maxPacketLength;
}