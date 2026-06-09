package mining.expanded;

public class Mission {
    private final String goldId;
    private final int x;
    private final int y;
    private int assignedAgent = -1;

    public Mission(String goldId, int x, int y) {
        this.goldId = goldId;
        this.x = x;
        this.y = y;
    }

    public String goldId() {
        return goldId;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int assignedAgent() {
        return assignedAgent;
    }

    public void assign(int assignedAgent) {
        this.assignedAgent = assignedAgent;
    }
}
