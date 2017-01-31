package massim.scenario.city.data;

/**
 * Stores some team info.
 */
public class TeamState {

    private long money;

    private String name;

    public TeamState(long money, String name){
        this.money = money;
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public long getMoney(){
        return money;
    }

    public void addMoney(long amount){
        money += amount;
    }

    public void subMoney(long amount){
        money -= amount;
    }
}
