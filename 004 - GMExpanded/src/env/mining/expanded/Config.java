package mining.expanded;

public final class Config {
    public static final int NB_AGENTS = 4;

    public static final int BASE_EQUIPMENT_SLOTS = 2;
    public static final int BASE_GOLD_CAPACITY = 1;
    public static final int BACKPACK_GOLD_BONUS = 2;
    public static final int CART_GOLD_BONUS = 4;

    public static final int BASE_VIEW_RADIUS = 1;
    public static final int LANTERN_VIEW_RADIUS = 3;
    public static final int BASE_SERVICE_RADIUS = 2;

    public static final int MOVE_COST = 1;
    public static final int CART_MOVE_COST = 2;
    public static final int MESSAGE_COST = 1;
    public static final int EXTRACT_COST = 2;
    public static final int DEPOSIT_COST = 1;
    public static final int EQUIP_COST = 1;
    public static final int SKIP_COST = 1;

    public static final int GUI_SLEEP_MS = 80;
    public static final long MAX_SIMULATION_MILLIS = 5L * 60L * 1000L;

    private Config() {
    }
}
