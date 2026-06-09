package mining.expanded;

public class AgentState {
    private final int id;
    private final Inventory inventory = new Inventory();
    private int depositedGold;
    private String currentMission = "none";

    public AgentState(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public String name() {
        return "miner" + (id + 1);
    }

    public Inventory inventory() {
        return inventory;
    }

    public int depositedGold() {
        return depositedGold;
    }

    public void addDepositedGold(int amount) {
        depositedGold += amount;
    }

    public String currentMission() {
        return currentMission;
    }

    public void setCurrentMission(String currentMission) {
        this.currentMission = currentMission;
    }
}
