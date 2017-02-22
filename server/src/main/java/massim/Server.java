package massim;

import massim.config.ServerConfig;
import massim.config.TeamConfig;
import massim.protocol.messagecontent.Action;
import massim.protocol.messagecontent.RequestAction;
import massim.protocol.messagecontent.SimEnd;
import massim.protocol.messagecontent.SimStart;
import massim.scenario.AbstractSimulation;
import massim.util.JSONUtil;
import massim.util.Log;
import massim.util.RNG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created in 2017.
 * MASSim server main class/entry point.
 * @author ta10
 */
public class Server {

    private Vector<String> commandQueue = new Vector<>();
    private ServerConfig config;

    private LoginManager loginManager;
    private AgentManager agentManager;

    /**
     * whether server should stop after the next match (random mode)
     */
    private boolean stopped = false;

    public static void main(String[] args){
        Server server = new Server();

        // parse command line arguments
        for (int i = 0; i < args.length; i++) {
            switch (args[i]){

                case "-conf":
                    try {
                        server.config = parseServerConfig(new JSONObject(new String(Files.readAllBytes(Paths.get(args[++i])),
                                StandardCharsets.UTF_8)));
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                        Log.log(Log.Level.ERROR, "Could not read massim.config file.");
                        i--;
                    }
                    break;
                case "-confString":
                    try {
                        server.config = parseServerConfig(new JSONObject(args[++i]));
                    } catch (JSONException e) {
                        Log.log(Log.Level.ERROR, "Passed configuration string invalid.");
                        i--;
                    }
                    break;
                default:
                    Log.log(Log.Level.ERROR, "Unknown option: " + args[i]);
            }
        }

        // ask to choose massim.config file from conf directory
        if (server.config == null){
            File confDir = new File("conf");
            confDir.mkdirs();
            File[] confFiles = confDir.listFiles();
            if (confFiles == null || confFiles.length == 0) {
                Log.log(Log.Level.NORMAL, "No massim.config files to load - exit MASSim.");
                System.exit(0);
            }
            else {
                Log.log(Log.Level.NORMAL, "Choose a number:");
                for (int i = 0; i < confFiles.length; i++) {
                    Log.log(Log.Level.NORMAL, i + " " + confFiles[i]);
                }
                Scanner in = new Scanner(System.in);
                Integer confNum = null;
                while (confNum == null) {
                    try {
                        confNum = Integer.parseInt(in.next());
                        if (confNum < 0 || confNum > confFiles.length - 1){
                            Log.log(Log.Level.NORMAL, "No massim.config for that number, try again:");
                            confNum = null;
                        }
                    } catch (Exception e) {
                        Log.log(Log.Level.NORMAL, "Invalid number, try again:");
                    }
                }
                try {
                    server.config = parseServerConfig(
                            new JSONObject(new String( Files.readAllBytes(Paths.get(confFiles[confNum].toURI())))));
                } catch (IOException e) {
                    Log.log(Log.Level.ERROR, "Could not read massim.config file, exiting MASSim");
                    System.exit(0);
                }
            }
        }

        server.go();
        server.close();
    }

    /**
     * Cleanup all threads etc.
     */
    private void close() {
        if (loginManager != null) loginManager.stop();
        if (agentManager != null) agentManager.stop();
    }

    /**
     * Starts server operation according to its configuration.
     */
    private void go(){

        //setup logging
        switch(config.logLevel){
            case "debug": Log.setLogLevel(Log.Level.DEBUG); break;
            case "error": Log.setLogLevel(Log.Level.ERROR); break;
            case "critical": Log.setLogLevel(Log.Level.CRITICAL); break;
            default: Log.setLogLevel(Log.Level.NORMAL);
        }
        if(config.logPath != null){
            File logFile = new File(config.logPath + File.separator + "MASSim-log-" + timestamp() + ".log");
            File dir = logFile.getParentFile();
            if(!dir.exists()) dir.mkdirs();
        }

        // setup backend
        agentManager = new AgentManager(config.teams, config.agentTimeout);
        try {
            loginManager = new LoginManager(agentManager, config.port, config.backlog);
            loginManager.start();
        } catch (IOException e) {
            Log.log(Log.Level.CRITICAL, "Cannot open server socket.");
            return;
        }

        // delay tournament start according to launch type
        if (config.launch.equals("key")){
            Log.log(Log.Level.NORMAL,"Please press ENTER to start the tournament.");
            try {
                System.in.read();
            } catch (IOException ignored) {}
        }
        else if(config.launch.endsWith("s")){
            try{
                int interval = Integer.parseInt(config.launch.substring(0, config.launch.length() - 1));
                Log.log(Log.Level.NORMAL, "Starting tournament in " + interval + " seconds.");
                Thread.sleep(interval * 1000);
            } catch(Exception e){
                Log.log(Log.Level.ERROR, "Failed waiting, starting tournament now.");
            }
        }
        else{
            DateFormat timeFormat = new SimpleDateFormat("HH:mm");
            Calendar cal = Calendar.getInstance();
            Log.log(Log.Level.NORMAL, "Current time is: " + cal.getTime().toString());
            try {
                //TODO review this part
                Calendar startDate = Calendar.getInstance();
                startDate.setTime(timeFormat.parse(config.launch));
                int hourOfDay = startDate.get(Calendar.HOUR_OF_DAY);
                int minute = startDate.get(Calendar.MINUTE);
                startDate.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), hourOfDay, minute);
                Log.log(Log.Level.NORMAL,"Starting time: " + startDate.getTime().toString());
                long time = startDate.getTimeInMillis();
                long diffTime = time - cal.getTimeInMillis();
                diffTime = Math.max(diffTime, 0);
                Log.log(Log.Level.NORMAL, "The tournament will start in " + diffTime/1000 + " seconds.");
                Thread.sleep(diffTime);
            } catch (Exception e) {
                Log.log(Log.Level.ERROR, "Could not parse start time. Starting tournament now.");
            }
        }

        // run matches according to tournament mode
        switch(config.tournamentMode){
            case ServerConfig.MODE_ROUND_ROBIN:
                // run a match for each team combination
                if (config.teamsPerMatch > config.teams.size()){
                    Log.log(Log.Level.ERROR, "Not enough teams configured. Stopping MASSim now.");
                    System.exit(0);
                }
                int[] indices = IntStream.rangeClosed(0, config.teamsPerMatch - 1).toArray();
                boolean nextMatch = true;
                while (nextMatch){
                    Set<TeamConfig> matchTeams = new HashSet<>();
                    for (int index : indices) matchTeams.add(config.teams.get(index));

                    runMatch(matchTeams);

                    // determine the next team constellation
                    for (int i = indices.length - 1; i >= 0; i--) {
                        if (indices[i] < config.teams.size() - 1 - (indices.length - 1 - i)){
                            indices[i]++;
                            for (int j = i + 1; j < indices.length; j++){
                                indices[j] = indices[i] + (j - i);
                            }
                            break;
                        }
                        if (i == 0) nextMatch = false; // no team constellation left
                    }
                }
                break;
            case ServerConfig.MODE_MANUAL:
                if(config.manualModeTeams != null) config.manualModeTeams.forEach(this::runMatch);
                break;
            case ServerConfig.MODE_RANDOM:
                while(!stopped){
                    List<TeamConfig> teams = new Vector<>(config.teams);
                    RNG.shuffle(teams);
                    runMatch(new HashSet<>(teams.subList(0, config.teamsPerMatch)));
                }
                break;
            default:
                Log.log(Log.Level.ERROR, "Invalid tournament mode: " + config.tournamentMode);
        }
    }

    /**
     * @return a string representation of the current time in the form yyyy-MM-dd-HH-mm-ss
     */
    private String timestamp() {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
    }


    /**
     * Stops running matches after the current one finishes (random tournament mode)
     */
    public void stop(){
        stopped = true;
    }

    /**
     * Runs a match for the given teams. Sim configuration is taken from the server config.
     * @param matchTeams a set of all teams to participate in the simulation
     */
    private void runMatch(Set<TeamConfig> matchTeams) {

        JSONObject result = new JSONObject();
        for (JSONObject simConfig: config.simConfigs){
            // create and run scenario instance with the given teams
            String className = simConfig.optString("scenarioClass", "");
            if (className.equals("")){
                Log.log(Log.Level.ERROR, "No scenario class specified.");
                continue;
            }
            try {
                AbstractSimulation sim = (AbstractSimulation) AbstractSimulation.class.getClassLoader()
                                                                .loadClass("massim.scenario." + className)
                                                                .newInstance();
                int steps = simConfig.optInt("steps", 1000);
                Map<String, SimStart> initialPercepts = sim.init(steps, simConfig, matchTeams);
                agentManager.handleInitialPercepts(initialPercepts);
                for (int i = 0; i < steps; i++){
                    Log.log(Log.Level.NORMAL, "Simulation at step " + i);
                    Map<String, RequestAction> percepts = sim.preStep(i);
                    Map<String, Action> actions = agentManager.requestActions(percepts);
                    sim.step(i, actions); // execute step with agent actions
                }
                Map<String, SimEnd> finalPercepts = sim.finish();
                agentManager.handleFinalPercepts(finalPercepts);
                result.put(sim.getName(), sim.getResult());
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                Log.log(Log.Level.ERROR, "Could not load scenario class: " + className);
            }
        }

        // write match result to file
        JSONUtil.writeToFile(result, new File(config.resultPath + File.separator + "result_" + timestamp()));
    }

    /**
     * Parses the given JSONObject into a new {@link ServerConfig} object.
     * Uses default values if the JSONObject is "broken" somehow.
     * @param conf the JSONObject (configuration) holding a "server" JSONObject
     * @return the parsed server config
     */
    public static ServerConfig parseServerConfig(JSONObject conf){
        ServerConfig config = new ServerConfig();
        JSONObject serverJSON = conf.optJSONObject("server");
        if (serverJSON == null) {
            Log.log(Log.Level.ERROR, "No server object in configuration.");
            serverJSON = new JSONObject();
        }
        config.launch = serverJSON.optString("launch", "key");
        Log.log(Log.Level.NORMAL, "Configuring launch type: " + config.launch);
        config.tournamentMode = serverJSON.optString("tournamentMode", "round-robin");
        Log.log(Log.Level.NORMAL, "Configuring tournament mode: " + config.tournamentMode);
        config.teamSize = serverJSON.optInt("teamSize", 16);
        Log.log(Log.Level.NORMAL, "Configuring team size: " + config.teamSize);
        config.teamsPerMatch = serverJSON.optInt("teamsPerMatch", 2);
        Log.log(Log.Level.NORMAL, "Configuring teams per match: " + config.teamsPerMatch);
        config.port = serverJSON.optInt("port", 12300);
        Log.log(Log.Level.NORMAL, "Configuring port: " + config.port);
        config.backlog = serverJSON.optInt("backlog", 10000);
        Log.log(Log.Level.NORMAL, "Configuring backlog: " + config.backlog);
        config.agentTimeout = serverJSON.optInt("agentTimeout", 4000);
        Log.log(Log.Level.NORMAL, "Configuring agent timeout: " + config.agentTimeout);
        config.logPath = serverJSON.optString("logPath");
        Log.log(Log.Level.NORMAL, "Configuring log path: " + config.logPath);
        config.logLevel = serverJSON.optString("logLevel", "normal");
        Log.log(Log.Level.NORMAL, "Configuring log level: " + config.logLevel);
        config.resultPath = serverJSON.optString("resultPath", "results");
        Log.log(Log.Level.NORMAL, "Configuring result path: " + config.resultPath);

        // parse teams
        JSONObject teamJSON = conf.optJSONObject("teams");
        Set<String> allAgents = new HashSet<>();
        if (teamJSON == null) Log.log(Log.Level.ERROR, "No teams configured.");
        else{
            teamJSON.keySet().forEach(name -> {
                TeamConfig team = new TeamConfig(name);
                config.teams.add(team);
                JSONObject accounts = teamJSON.optJSONObject(name);
                if (accounts != null){
                    accounts.keySet().forEach(agName -> {
                        if(!allAgents.add(agName))
                            Log.log(Log.Level.CRITICAL, "Agent " + agName + " occurs in multiple teams.");
                        team.addAgent(agName, accounts.getString(agName));
                        config.accounts.put(agName, accounts.getString(agName));
                    });
                }
            });
        }

        // parse matches
        JSONArray matchJSON = conf.optJSONArray("match");
        if (matchJSON == null){
            Log.log(Log.Level.ERROR, "No match configured.");
            System.exit(0);
        }
        for(int i = 0; i < matchJSON.length(); i++){
            JSONObject simConfig = matchJSON.optJSONObject(i);
            if (simConfig != null) {
                config.simConfigs.add(simConfig);
            }
        }

        // parse manual mode config (if required)
        if(config.tournamentMode.equals(ServerConfig.MODE_MANUAL)){
            Map<String, TeamConfig> teamMap = config.teams.stream()
                    .collect(Collectors.toMap(TeamConfig::getName, t -> t));
            List<Set<TeamConfig>> matchTeams = new Vector<>();
            JSONArray manualConf = conf.optJSONArray("manual-mode");
            if (manualConf == null){
                Log.log(Log.Level.CRITICAL, "No teams configured for manual mode. Exiting.");
                System.exit(0);
            }
            for(int i = 0; i < manualConf.length(); i++){
                JSONArray teamList = manualConf.optJSONArray(i);
                if(teamList != null){
                    Set<TeamConfig> parsedTeamNames = new HashSet<>();
                    for (int j = 0; j < config.teamsPerMatch; j++){
                        String team = teamList.optString(i, "");
                        if (team.equals("")) break;
                        TeamConfig teamConfig = teamMap.get(team);

                        if(teamConfig != null) parsedTeamNames.add(teamMap.get(team));
                        else Log.log(Log.Level.ERROR, "No team with name " + team + " configured.");
                    }
                    if (parsedTeamNames.size() == config.teamsPerMatch) matchTeams.add(parsedTeamNames);
                }
            }
            config.manualModeTeams = matchTeams;
        }
        return config;
    }
}