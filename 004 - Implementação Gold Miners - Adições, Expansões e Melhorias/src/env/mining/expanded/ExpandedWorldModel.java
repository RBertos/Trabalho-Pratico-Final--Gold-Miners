package mining.expanded;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import mining.expanded.ExpandedMiningPlanet.Move;

public class ExpandedWorldModel extends GridWorldModel {
    public static final int GOLD = 16;
    public static final int DEPOT = 32;
    public static final int ENEMY = 64;

    private static final Logger logger = Logger.getLogger(ExpandedWorldModel.class.getName());
    private static ExpandedWorldModel model;

    private final AgentState[] agents;
    private final Map<EquipmentType, Integer> baseEquipment = new EnumMap<>(EquipmentType.class);
    private final List<String> eventLog = new ArrayList<>();

    private Location depot;
    private int initialNbGolds;
    private int goldsInDepot;
    private long tick;
    private String id = "Expanded Gold Miners";
    private String lastEvent = "simulation initialized";

    private ExpandedWorldModel(int w, int h, int nbAgs) {
        super(w, h, nbAgs);
        agents = new AgentState[nbAgs];
        for (int i = 0; i < nbAgs; i++) {
            agents[i] = new AgentState(i);
        }
        for (EquipmentType type : EquipmentType.values()) {
            baseEquipment.put(type, 0);
        }
    }

    public static synchronized ExpandedWorldModel create(int w, int h, int nbAgs) {
        if (model == null) {
            model = new ExpandedWorldModel(w, h, nbAgs);
        }
        return model;
    }

    public static synchronized ExpandedWorldModel get() {
        return model;
    }

    public static synchronized void destroy() {
        model = null;
    }

    public static ExpandedWorldModel scenario(int scenario) throws Exception {
        switch (scenario) {
            case 1:
                return world1();
            case 2:
                return world2();
            case 3:
                return world3();
            case 4:
                return world4();
            case 5:
                return world5();
            case 6:
            default:
                return world6();
        }
    }

    public synchronized AgentState agent(int ag) {
        return agents[ag];
    }

    public synchronized long tick() {
        return tick;
    }

    public synchronized void consumeTicks(int cost, String reason) {
        tick += Math.max(0, cost);
        recordEvent("tick +" + cost + " " + reason + " => " + tick);
    }

    public synchronized String lastEvent() {
        return lastEvent;
    }

    public synchronized List<String> eventLogSnapshot() {
        return new ArrayList<>(eventLog);
    }

    public synchronized String getId() {
        return id;
    }

    public synchronized void setId(String id) {
        this.id = id;
    }

    public synchronized Location getDepot() {
        return depot;
    }

    public synchronized int getGoldsInDepot() {
        return goldsInDepot;
    }

    public synchronized int getInitialNbGolds() {
        return initialNbGolds;
    }

    public synchronized boolean isCarryingGold(int ag) {
        return agents[ag].inventory().carriedGold() > 0;
    }

    public synchronized int carryingGold(int ag) {
        return agents[ag].inventory().carriedGold();
    }

    public synchronized int goldCapacity(int ag) {
        return agents[ag].inventory().goldCapacity();
    }

    public synchronized int viewRadius(int ag) {
        return agents[ag].inventory().viewRadius();
    }

    public synchronized String equipmentSummary(int ag) {
        return agents[ag].inventory().equipmentSummary();
    }

    public synchronized int depositedBy(int ag) {
        return agents[ag].depositedGold();
    }

    public synchronized int baseStock(EquipmentType type) {
        return baseEquipment.get(type);
    }

    public synchronized void addBaseStock(EquipmentType type, int amount) {
        baseEquipment.put(type, baseEquipment.get(type) + amount);
    }

    public synchronized boolean hasEquipment(int ag, EquipmentType type) {
        return agents[ag].inventory().has(type);
    }

    public synchronized void setDepot(int x, int y) {
        depot = new Location(x, y);
        data[x][y] |= DEPOT;
    }

    public synchronized void setInitialNbGolds(int initialNbGolds) {
        this.initialNbGolds = initialNbGolds;
    }

    public synchronized boolean move(Move dir, int ag) {
        Location l = getAgPos(ag);
        int nx = l.x;
        int ny = l.y;
        switch (dir) {
            case UP:
                ny--;
                break;
            case DOWN:
                ny++;
                break;
            case RIGHT:
                nx++;
                break;
            case LEFT:
                nx--;
                break;
            default:
                break;
        }

        consumeTicks(agents[ag].inventory().moveCost(), agents[ag].name() + " move " + dir);
        if (isEnterable(nx, ny, ag)) {
            setAgPos(ag, nx, ny);
            recordEvent(agents[ag].name() + " moved to (" + nx + "," + ny + ")");
            return true;
        }
        recordEvent(agents[ag].name() + " blocked moving to (" + nx + "," + ny + ")");
        return false;
    }

    private boolean isEnterable(int x, int y, int ag) {
        if (!inGrid(x, y) || hasObject(OBSTACLE, x, y)) {
            return false;
        }
        for (int i = 0; i < agents.length; i++) {
            if (i == ag) {
                continue;
            }
            Location other = getAgPos(i);
            if (other != null && other.x == x && other.y == y) {
                return false;
            }
        }
        return true;
    }

    public synchronized boolean pick(int ag) {
        consumeTicks(Config.EXTRACT_COST, agents[ag].name() + " extract");
        Location l = getAgPos(ag);
        Inventory inventory = agents[ag].inventory();
        if (!hasObject(GOLD, l.x, l.y)) {
            logger.warning(agents[ag].name() + " tried to pick gold where there is no gold at " + l.x + "x" + l.y);
            return false;
        }
        if (!inventory.canCarryMoreGold()) {
            logger.warning(agents[ag].name() + " tried to pick gold with full cargo");
            return false;
        }
        remove(GOLD, l.x, l.y);
        inventory.addGold();
        recordEvent(agents[ag].name() + " picked gold at (" + l.x + "," + l.y + ")");
        return true;
    }

    public synchronized int drop(int ag) {
        consumeTicks(Config.DEPOSIT_COST, agents[ag].name() + " deposit");
        Location l = getAgPos(ag);
        Inventory inventory = agents[ag].inventory();
        if (inventory.carriedGold() == 0) {
            return 0;
        }
        int dropped = inventory.dropAllGold();
        if (l.equals(depot)) {
            goldsInDepot += dropped;
            agents[ag].addDepositedGold(dropped);
            recordEvent(agents[ag].name() + " deposited " + dropped + " gold");
        } else {
            for (int i = 0; i < dropped; i++) {
                add(GOLD, l.x, l.y);
            }
            recordEvent(agents[ag].name() + " dropped gold outside depot");
        }
        return dropped;
    }

    public synchronized boolean equip(int ag, EquipmentType type) {
        consumeTicks(Config.EQUIP_COST, agents[ag].name() + " equip " + type.atom());
        if (!getAgPos(ag).equals(depot)) {
            recordEvent(agents[ag].name() + " failed to equip outside depot");
            return false;
        }
        int stock = baseEquipment.get(type);
        if (stock <= 0 || !agents[ag].inventory().equip(type)) {
            recordEvent(agents[ag].name() + " failed to equip " + type.atom());
            return false;
        }
        baseEquipment.put(type, stock - 1);
        recordEvent(agents[ag].name() + " equipped " + type.atom());
        return true;
    }

    public synchronized boolean unequip(int ag, EquipmentType type) {
        consumeTicks(Config.EQUIP_COST, agents[ag].name() + " unequip " + type.atom());
        if (!getAgPos(ag).equals(depot)) {
            recordEvent(agents[ag].name() + " failed to unequip outside depot");
            return false;
        }
        if (!agents[ag].inventory().unequip(type)) {
            recordEvent(agents[ag].name() + " failed to unequip " + type.atom());
            return false;
        }
        baseEquipment.put(type, baseEquipment.get(type) + 1);
        recordEvent(agents[ag].name() + " stored " + type.atom() + " at depot");
        return true;
    }

    public synchronized void skip(int ag) {
        consumeTicks(Config.SKIP_COST, agents[ag].name() + " skip");
    }

    public synchronized void commTick(int ag, String label) {
        consumeTicks(Config.MESSAGE_COST, agents[ag].name() + " broadcast " + label);
    }

    public synchronized void setMission(int ag, String mission) {
        agents[ag].setCurrentMission(mission);
        recordEvent(agents[ag].name() + " mission " + mission);
    }

    public synchronized String mission(int ag) {
        return agents[ag].currentMission();
    }

    public synchronized void addGold(int x, int y) {
        add(GOLD, x, y);
        initialNbGolds++;
        recordEvent("gold added at (" + x + "," + y + ")");
    }

    public synchronized void recordEvent(String event) {
        lastEvent = event;
        eventLog.add(event);
        if (eventLog.size() > 12) {
            eventLog.remove(0);
        }
    }

    public synchronized String baseEquipmentSummary() {
        return "lanterna=" + baseStock(EquipmentType.LANTERN)
                + " carrinho=" + baseStock(EquipmentType.CART)
                + " mochila=" + baseStock(EquipmentType.BACKPACK);
    }

    public synchronized String toString() {
        return id;
    }

    private static void addCommonObstacles(ExpandedWorldModel model) {
        int[][] obstacles = {
                {12, 3}, {13, 3}, {14, 3}, {15, 3}, {18, 3}, {19, 3}, {20, 3},
                {14, 8}, {15, 8}, {16, 8}, {17, 8}, {19, 8}, {20, 8},
                {12, 32}, {13, 32}, {14, 32}, {15, 32}, {18, 32}, {19, 32}, {20, 32},
                {14, 28}, {15, 28}, {16, 28}, {17, 28}, {19, 28}, {20, 28},
                {3, 12}, {3, 13}, {3, 14}, {3, 15}, {3, 18}, {3, 19}, {3, 20},
                {8, 14}, {8, 15}, {8, 16}, {8, 17}, {8, 19}, {8, 20},
                {32, 12}, {32, 13}, {32, 14}, {32, 15}, {32, 18}, {32, 19}, {32, 20},
                {28, 14}, {28, 15}, {28, 16}, {28, 17}, {28, 19}, {28, 20},
                {13, 13}, {13, 14}, {13, 16}, {13, 17}, {13, 19}, {14, 19},
                {16, 19}, {17, 19}, {19, 19}, {19, 18}, {19, 16}, {19, 15},
                {19, 13}, {18, 13}, {16, 13}, {15, 13},
                {2, 32}, {3, 32}, {4, 32}, {5, 32}, {6, 32}, {7, 32}, {8, 32}, {9, 32}, {10, 32},
                {10, 31}, {10, 30}, {10, 29}, {10, 28}, {10, 27}, {10, 26}, {10, 25}, {10, 24}, {10, 23},
                {2, 23}, {3, 23}, {4, 23}, {5, 23}, {6, 23}, {7, 23}, {8, 23}, {9, 23},
                {2, 29}, {2, 28}, {2, 27}, {2, 26}, {2, 25}, {2, 24},
                {3, 29}, {4, 29}, {5, 29}, {6, 29}, {7, 29}, {7, 28}, {7, 27}, {7, 26}, {7, 25},
                {6, 25}, {5, 25}, {4, 25}, {4, 26}, {4, 27}
        };
        for (int[] obstacle : obstacles) {
            model.add(OBSTACLE, obstacle[0], obstacle[1]);
        }
    }

    private static void addCommonGold(ExpandedWorldModel model) {
        int[][] golds = {
                {20, 13}, {15, 20}, {1, 1}, {3, 5}, {24, 24}, {20, 20},
                {20, 21}, {2, 22}, {2, 12}, {19, 2}, {14, 4}, {34, 34}
        };
        for (int[] gold : golds) {
            model.add(GOLD, gold[0], gold[1]);
        }
    }

    private static void initEquipment(ExpandedWorldModel model) {
        model.agent(0).inventory().equip(EquipmentType.LANTERN);
        model.agent(1).inventory().equip(EquipmentType.CART);
        model.agent(2).inventory().equip(EquipmentType.BACKPACK);
        model.agent(3).inventory().equip(EquipmentType.LANTERN);
        model.agent(3).inventory().equip(EquipmentType.BACKPACK);
        model.addBaseStock(EquipmentType.LANTERN, 1);
        model.addBaseStock(EquipmentType.CART, 1);
        model.addBaseStock(EquipmentType.BACKPACK, 1);
    }

    private static void finishSetup(ExpandedWorldModel model) {
        model.setInitialNbGolds(model.countObjects(GOLD));
        initEquipment(model);
    }

    static ExpandedWorldModel world1() throws Exception {
        ExpandedWorldModel model = ExpandedWorldModel.create(21, 21, Config.NB_AGENTS);
        model.setId("Expanded Scenario 1");
        model.setDepot(0, 0);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 20, 0);
        model.setAgPos(2, 3, 20);
        model.setAgPos(3, 20, 20);
        finishSetup(model);
        return model;
    }

    static ExpandedWorldModel world2() throws Exception {
        ExpandedWorldModel model = ExpandedWorldModel.create(35, 35, Config.NB_AGENTS);
        model.setId("Expanded Scenario 2");
        model.setDepot(0, 0);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 22, 0);
        model.setAgPos(2, 3, 22);
        model.setAgPos(3, 22, 22);
        for (int y = 0; y <= 20; y++) {
            model.add(OBSTACLE, 20, y);
        }
        for (int x = 21; x < 35; x++) {
            model.add(OBSTACLE, x, 20);
        }
        finishSetup(model);
        return model;
    }

    static ExpandedWorldModel world3() throws Exception {
        ExpandedWorldModel model = ExpandedWorldModel.create(35, 35, Config.NB_AGENTS);
        model.setId("Expanded Scenario 3");
        model.setDepot(0, 0);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 20, 0);
        model.setAgPos(2, 3, 20);
        model.setAgPos(3, 20, 20);
        addCommonGold(model);
        finishSetup(model);
        return model;
    }

    static ExpandedWorldModel world4() throws Exception {
        ExpandedWorldModel model = ExpandedWorldModel.create(35, 35, Config.NB_AGENTS);
        model.setId("Expanded Scenario 4");
        model.setDepot(5, 27);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 20, 0);
        model.setAgPos(2, 3, 20);
        model.setAgPos(3, 20, 20);
        addCommonGold(model);
        finishSetup(model);
        return model;
    }

    static ExpandedWorldModel world5() throws Exception {
        ExpandedWorldModel model = world4();
        model.setId("Expanded Scenario 5");
        int[][] obstacles = {
                {12, 3}, {13, 3}, {14, 3}, {15, 3}, {18, 3}, {19, 3}, {20, 3},
                {14, 8}, {15, 8}, {16, 8}, {17, 8}, {19, 8}, {20, 8}
        };
        for (int[] obstacle : obstacles) {
            model.add(OBSTACLE, obstacle[0], obstacle[1]);
        }
        return model;
    }

    static ExpandedWorldModel world6() throws Exception {
        ExpandedWorldModel model = ExpandedWorldModel.create(35, 35, Config.NB_AGENTS);
        model.setId("Expanded Scenario 6");
        model.setDepot(16, 16);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 20, 0);
        model.setAgPos(2, 6, 26);
        model.setAgPos(3, 20, 20);
        addCommonGold(model);
        addCommonObstacles(model);
        finishSetup(model);
        return model;
    }
}
