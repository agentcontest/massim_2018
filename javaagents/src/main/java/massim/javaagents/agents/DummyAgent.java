package massim.javaagents.agents;

import eis.iilang.*;
import massim.javaagents.MailService;

import java.util.*;

/**
 * A DummyAgent Created in 2018 for the 2018 City scenario.
 */
public class DummyAgent extends Agent{

    private State state = State.EXPLORE;
    private Percept goal;

    private String role = "";
    private int charge = 0;
    private int battery = 0;
    private double lat = 0;
    private double lon = 0;
    private double minLat = 0;
    private double maxLat = 0;
    private double minLon = 0;
    private double maxLon = 0;
    private String lastAction = "";
    private String lastActionResult = "";
    private int massium = 0;
    private int score = 0;
    private int step = 0;

    private Map<String, Percept> resourceNodes = new HashMap<>();
    private Set<String> availableResources = new HashSet<>();
    private String wellName;
    private int wellCost = Integer.MAX_VALUE;
    private Map<String, Percept> chargingStations = new HashMap<>();

    private String leader = "";
    private Loc exploreTarget;
    private Loc chargingTarget;
    private int buildCounter;
    private int lastBuild = -100;
    private String currentJob = "";
    private Map<String, Percept> jobs = new HashMap<>();

    private Random rand = new Random(17);

    /**
     * Constructor
     *
     * @param name    the agent's name
     * @param mailbox the mail facility
     */
    public DummyAgent(String name, MailService mailbox) {
        super(name, mailbox);
    }

    @Override
    public void handlePercept(Percept percept) {}

    @Override
    public Action step() {

        // parse percepts
        List<Percept> percepts = getPercepts();
        for (Percept percept : percepts) {
            switch(percept.getName()) {
                case "job":
                    jobs.put(getStringParam(percept, 0), percept);
                    break;
                case "resourceNode":
                    broadcast(percept, getName());
                    resourceNodes.put(getStringParam(percept, 0), percept);
                    availableResources.add(getStringParam(percept, 3));
                    say("Found a resource node.");
                    break;
                case "role":
                    role = getStringParam(percept, 0);
                    battery = getIntParam(percept, 9);
                    break;
                case "charge":
                    charge = getIntParam(percept, 0);
                    break;
                case "minLat": minLat = getDoubleParam(percept, 0); break;
                case "maxLat": maxLat = getDoubleParam(percept, 0); break;
                case "minLon": minLon = getDoubleParam(percept, 0); break;
                case "maxLon": maxLon = getDoubleParam(percept, 0); break;
                case "lat": lat = getDoubleParam(percept, 0); break;
                case "lon": lon = getDoubleParam(percept, 0); break;
                case "lastAction": lastAction = getStringParam(percept, 0); break;
                case "lastActionResult": lastActionResult = getStringParam(percept, 0); break;
                case "wellType":
                    int cost = getIntParam(percept, 1);
                    if(cost < wellCost) {
                        wellCost = cost;
                        wellName = getStringParam(percept, 0);
                    }
                    break;
                case "massium": massium = getIntParam(percept, 0); break;
                case "score": score = getIntParam(percept, 0); break;
                case "chargingStation": chargingStations.put(getStringParam(percept, 0), percept); break;
                case "step": step = getIntParam(percept, 0); break;
            }
        }

        // "elect" a leader
        if(!role.equalsIgnoreCase("drone") && leader.equals("")) {
            broadcast(new Percept("leader"), getName());
            this.leader = getName();
        }

        say("My last action was " + lastAction + " : " + lastActionResult);

        if(leader.equals(getName())) say("Score: " + score + " Massium: " + massium);

        return act();
    }

    /**
     * choose an action
     * @return an action
     */
    private Action act() {

        if(getName().equals(leader) && currentJob.equals("")) {
            for(Percept job: jobs.values()) {
                int endStep = getIntParam(job, 4);
                if((endStep - step) > 100) {
                    ParameterList items = (ParameterList) job.getParameters().get(5);
                    // TODO distribute items among team members
                }
            }
        }

        if(charge < .4 * battery) {
            state = State.RECHARGE;
            String station = "";
            double minDist = Double.MAX_VALUE;
            for(Percept p: chargingStations.values()) {
                double cLat = getDoubleParam(p, 1);
                double cLon = getDoubleParam(p, 2);
                double dist = Math.sqrt(Math.pow(lat - cLat, 2) + Math.pow(lon - cLon, 2));
                if(dist < minDist) {
                    minDist = dist;
                    station = getStringParam(p, 0);
                }
            }
            if(!station.equals("")) {
                Percept p = chargingStations.get(station);
                chargingTarget = new Loc(getDoubleParam(p, 1), getDoubleParam(p, 2));
            }
        }

        if(state == State.RECHARGE) {
            if(charge > .8 * battery) {
                if(goal != null) {
                    // TODO resume goal
                }
                else {
                    state = State.EXPLORE;
                }
            }
            else {
                if (chargingTarget != null) {
                    if (atLoc(chargingTarget)) {
                        return new Action("charge");
                    }
                    else {
                        return new Action("goto", new Numeral(chargingTarget.lat), new Numeral(chargingTarget.lon));
                    }
                }
            }
        }

        if(leader.equals(getName())) {
            if(state == State.BUILD) {
                if(buildCounter-- == 0) {
                    state = State.EXPLORE;
                }
                else {
                    lastBuild = step;
                    return new Action("build");
                }
            }

            if(step - lastBuild > 30 && wellName != null && massium > wellCost) {
                state = State.BUILD;
                buildCounter = 20; // IMPROVE check actual progress
                return new Action("build", new Identifier(wellName));
            }
        }

        if(exploreTarget != null) {
            if(atLoc(exploreTarget)) {
                // target reached
                exploreTarget = null;
            }
        }

        if(state == State.EXPLORE) {
            if(exploreTarget == null || lastActionResult.equalsIgnoreCase("failed_no_route")) {
                double expLat = minLat + rand.nextDouble() * (maxLat - minLat);
                double expLon = minLon + rand.nextDouble() * (maxLon - minLon);
                exploreTarget = new Loc(expLat, expLon);
            }
            return new Action("goto", new Numeral(exploreTarget.lat), new Numeral(exploreTarget.lon));
        }

        return new Action("continue");
    }

    private boolean atLoc(Loc loc) {
        return Math.abs(lat - loc.lat) < .0001 && Math.abs(lon - loc.lon) < .0001;
    }

    private String getStringParam(Percept percept, int position) {
        Parameter p = percept.getParameters().get(position);
        if (p instanceof Identifier) return ((Identifier) p).getValue();
        return "";
    }

    private int getIntParam(Percept percept, int position) {
        Parameter p = percept.getParameters().get(position);
        if (p instanceof Numeral) return ((Numeral) p).getValue().intValue();
        return 0;
    }

    private double getDoubleParam(Percept percept, int position) {
        Parameter p = percept.getParameters().get(position);
        if (p instanceof Numeral) return ((Numeral) p).getValue().doubleValue();
        return 0;
    }

    @Override
    public void handleMessage(Percept message, String sender) {
        switch(message.getName()) {
            case "leader":
                this.leader = sender;
                say("I agree to " + sender + " being the group leader.");
                break;
            case "resourceNode":
                String name = ((Identifier)message.getParameters().get(0)).getValue();
                String resource = ((Identifier)message.getParameters().get(3)).getValue();
                resourceNodes.put(name, message);
                availableResources.add(resource);
                break;
            default:
                say("I cannot handle a message of type " + message.getName());
        }
    }

    class Loc {
        double lat;
        double lon;
        Loc(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }

    enum State {
        EXPLORE, RECHARGE, JOB, BUILD
    }
}
