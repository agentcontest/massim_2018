package massim.scenario.city.data;

/**
 * Stores some team info.
 */
public class TeamState {

    private long massium;
    private long score = 0;

    private String name;

    TeamState(long massium, String name){
        this.massium = massium;
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public long getMassium(){
        return massium;
    }

    public void addMassium(int amount){
        massium += amount;
    }

    public void subMassium(long amount){
        massium -= amount;
    }

    public void addScore(int points) {
        score += points;
    }

    public long getScore() {
        return score;
    }
}
