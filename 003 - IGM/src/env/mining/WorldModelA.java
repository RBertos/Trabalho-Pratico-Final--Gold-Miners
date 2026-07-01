package mining;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import mining.MiningPlanetA.Move;

public class WorldModelA extends GridWorldModel {

    public static final int COAL    = 16;
    public static final int IRON    = 128;
    public static final int GOLD    = 256;
    public static final int DIAMOND = 512;
    public static final int DEPOT   = 32;
    public static final int ENEMY   = 64;

    Location                  depot;
    Set<Integer>              agWithGold;  // which agent is carrying gold
    Map<Integer,Integer>      agCarryingOre;
    int                       goldsInDepot   = 0;
    int                       initialNbGolds = 0;

    private Logger            logger   = Logger.getLogger("jasonTeamSimLocal.mas2j." + WorldModelA.class.getName());

    private String            id = "WorldModelA";

    // singleton pattern
    protected static WorldModelA model = null;

    synchronized public static WorldModelA create(int w, int h, int nbAgs) {
        if (model == null) {
            model = new WorldModelA(w, h, nbAgs);
        }
        return model;
    }

    public static WorldModelA get() {
        return model;
    }

    public static void destroy() {
        model = null;
        LanternArtifact.clearLanterns();
    }

    private WorldModelA(int w, int h, int nbAgs) {
        super(w, h, nbAgs);
        agWithGold = new HashSet<Integer>();
        agCarryingOre = new HashMap<Integer,Integer>();
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String toString() {
        return id;
    }

    public Location getDepot() {
        return depot;
    }

    public int getGoldsInDepot() {
        return goldsInDepot;
    }

    public boolean isAllGoldsCollected() {
        return goldsInDepot == initialNbGolds;
    }

    public void setInitialNbGolds(int i) {
        initialNbGolds = i;
    }

    public int getInitialNbGolds() {
        return initialNbGolds;
    }

    public boolean isCarryingGold(int ag) {
        return agWithGold.contains(ag);
    }

    public int getCarriedOre(int ag) {
        Integer ore = agCarryingOre.get(ag);
        return ore == null ? 0 : ore;
    }

    public void setDepot(int x, int y) {
        depot = new Location(x, y);
        data[x][y] = DEPOT;
    }

    public void setAgCarryingGold(int ag) {
        agWithGold.add(ag);
    }
    public void setAgCarryingOre(int ag, int ore) {
        agWithGold.add(ag);
        agCarryingOre.put(ag, ore);
    }
    public void setAgNotCarryingGold(int ag) {
        agWithGold.remove(ag);
        agCarryingOre.remove(ag);
    }

    /** Actions **/

    boolean move(Move dir, int ag) throws Exception {
        Location l = getAgPos(ag);
        switch (dir) {
        case UP:
            if (isFree(l.x, l.y - 1)) {
                setAgPos(ag, l.x, l.y - 1);
            }
            break;
        case DOWN:
            if (isFree(l.x, l.y + 1)) {
                setAgPos(ag, l.x, l.y + 1);
            }
            break;
        case RIGHT:
            if (isFree(l.x + 1, l.y)) {
                setAgPos(ag, l.x + 1, l.y);
            }
            break;
        case LEFT:
            if (isFree(l.x - 1, l.y)) {
                setAgPos(ag, l.x - 1, l.y);
            }
            break;
        }
        return true;
    }

    boolean pick(int ag) {
        Location l = getAgPos(ag);
        int ore = getOreAt(l.x, l.y);
        if (ore != 0) {
            if (!isCarryingGold(ag)) {
                remove(ore, l.x, l.y);
                setAgCarryingOre(ag, ore);
                return true;
            } else {
                logger.warning("Agent " + (ag + 1) + " is trying the pick ore, but it is already carrying ore!");
            }
        } else {
            logger.warning("Agent " + (ag + 1) + " is trying the pick ore, but there is no ore at " + l.x + "x" + l.y + "!");
        }
        return false;
    }

    boolean drop(int ag) {
        Location l = getAgPos(ag);
        if (isCarryingGold(ag)) {
            if (l.equals(getDepot())) {
                goldsInDepot++;
                logger.info("Agent " + (ag + 1) + " carried an ore to depot!");
            } else {
                add(agCarryingOre.get(ag), l.x, l.y);
            }
            setAgNotCarryingGold(ag);
            return true;
        }
        return false;
    }

    private int getOreAt(int x, int y) {
        if (hasObject(WorldModelA.COAL, x, y)) return WorldModelA.COAL;
        if (hasObject(WorldModelA.IRON, x, y)) return WorldModelA.IRON;
        if (hasObject(WorldModelA.GOLD, x, y)) return WorldModelA.GOLD;
        if (hasObject(WorldModelA.DIAMOND, x, y)) return WorldModelA.DIAMOND;
        return 0;
    }

    /*
    public void clearAgView(int agId) {
        clearAgView(getAgPos(agId).x, getAgPos(agId).y);
    }

    public void clearAgView(int x, int y) {
        int e1 = ~(ENEMY + ALLY + GOLD);
        if (x > 0 && y > 0) {
            data[x - 1][y - 1] &= e1;
        } // nw
        if (y > 0) {
            data[x][y - 1] &= e1;
        } // n
        if (x < (width - 1) && y > 0) {
            data[x + 1][y - 1] &= e1;
        } // ne

        if (x > 0) {
            data[x - 1][y] &= e1;
        } // w
        data[x][y] &= e1; // cur
        if (x < (width - 1)) {
            data[x + 1][y] &= e1;
        } // e

        if (x > 0 && y < (height - 1)) {
            data[x - 1][y + 1] &= e1;
        } // sw
        if (y < (height - 1)) {
            data[x][y + 1] &= e1;
        } // s
        if (x < (width - 1) && y < (height - 1)) {
            data[x + 1][y + 1] &= e1;
        } // se
    }
    */


    /** no gold/no obstacle world */
    static WorldModelA world1() throws Exception {
        WorldModelA model = WorldModelA.create(21, 21, 4);
        model.setId("Scenario 1");
        model.setDepot(0, 0);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 20, 0);
        model.setAgPos(2, 3, 20);
        model.setAgPos(3, 20, 20);
        model.setInitialNbGolds(model.countObjects(WorldModelA.GOLD));
        return model;
    }

    /** world with gold, no obstacle */
    static WorldModelA world2() throws Exception {
        WorldModelA model = WorldModelA.create(35, 35, 4);
        model.setId("Scenario 2");
        model.setDepot(0, 0);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 22, 0);
        model.setAgPos(2, 3, 22);
        model.setAgPos(3, 22, 22);
        model.add(WorldModelA.OBSTACLE, 20, 0);
        model.add(WorldModelA.OBSTACLE, 20, 1);
        model.add(WorldModelA.OBSTACLE, 20, 2);
        model.add(WorldModelA.OBSTACLE, 20, 3);
        model.add(WorldModelA.OBSTACLE, 20, 4);
        model.add(WorldModelA.OBSTACLE, 20, 5);
        model.add(WorldModelA.OBSTACLE, 20, 6);
        model.add(WorldModelA.OBSTACLE, 20, 7);
        model.add(WorldModelA.OBSTACLE, 20, 8);
        model.add(WorldModelA.OBSTACLE, 20, 9);
        model.add(WorldModelA.OBSTACLE, 20, 10);
        model.add(WorldModelA.OBSTACLE, 20, 11);
        model.add(WorldModelA.OBSTACLE, 20, 12);
        model.add(WorldModelA.OBSTACLE, 20, 13);
        model.add(WorldModelA.OBSTACLE, 20, 14);
        model.add(WorldModelA.OBSTACLE, 20, 15);
        model.add(WorldModelA.OBSTACLE, 20, 16);
        model.add(WorldModelA.OBSTACLE, 20, 17);
        model.add(WorldModelA.OBSTACLE, 20, 18);
        model.add(WorldModelA.OBSTACLE, 20, 19);
        model.add(WorldModelA.OBSTACLE, 20, 20);
        model.add(WorldModelA.OBSTACLE, 21, 20);
        model.add(WorldModelA.OBSTACLE, 22, 20);
        model.add(WorldModelA.OBSTACLE, 23, 20);
        model.add(WorldModelA.OBSTACLE, 24, 20);
        model.add(WorldModelA.OBSTACLE, 25, 20);
        model.add(WorldModelA.OBSTACLE, 26, 20);
        model.add(WorldModelA.OBSTACLE, 27, 20);
        model.add(WorldModelA.OBSTACLE, 28, 20);
        model.add(WorldModelA.OBSTACLE, 29, 20);
        model.add(WorldModelA.OBSTACLE, 30, 20);
        model.add(WorldModelA.OBSTACLE, 31, 20);
        model.add(WorldModelA.OBSTACLE, 32, 20);
        model.add(WorldModelA.OBSTACLE, 33, 20);
        model.add(WorldModelA.OBSTACLE, 34, 20);
        model.setInitialNbGolds(model.countObjects(WorldModelA.GOLD));
        return model;
    }

    /** world with gold, no obstacle */
    static WorldModelA world3() throws Exception {
        WorldModelA model = WorldModelA.create(35, 35, 4);
        model.setId("Scenario 3");
        model.setDepot(0, 0);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 20, 0);
        model.setAgPos(2, 3, 20);
        model.setAgPos(3, 20, 20);
        model.add(WorldModelA.GOLD, 20, 13);
        model.add(WorldModelA.GOLD, 15, 20);
        model.add(WorldModelA.GOLD, 1, 1);
        model.add(WorldModelA.GOLD, 3, 5);
        model.add(WorldModelA.GOLD, 24, 24);
        model.add(WorldModelA.GOLD, 20, 20);
        model.add(WorldModelA.GOLD, 26, 21);
        model.add(WorldModelA.GOLD, 12, 22);
        model.add(WorldModelA.GOLD, 20, 23);
        model.add(WorldModelA.GOLD, 33, 24);
        model.add(WorldModelA.GOLD, 19, 20);
        model.add(WorldModelA.GOLD, 19, 21);
        model.add(WorldModelA.GOLD, 34, 34);
        model.setInitialNbGolds(model.countObjects(WorldModelA.GOLD));
        return model;
    }



    /** world with gold, no obstacle */
    static WorldModelA world4() throws Exception {
        WorldModelA model = WorldModelA.create(35, 35, 4);
        model.setId("Scenario 4");
        model.setDepot(5, 27);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 4, 0);
        model.setAgPos(2, 8, 0);
        model.setAgPos(3, 16, 0);
        model.add(WorldModelA.COAL, 3, 9);
        model.add(WorldModelA.IRON, 10, 8);
        model.add(WorldModelA.DIAMOND, 23, 25);
        model.add(WorldModelA.GOLD, 20, 13);
        model.add(WorldModelA.GOLD, 15, 20);
        model.add(WorldModelA.GOLD, 7, 7);
        model.add(WorldModelA.GOLD, 3, 5);
        model.add(WorldModelA.GOLD, 24, 24);
        model.add(WorldModelA.GOLD, 20, 20);
        model.add(WorldModelA.GOLD, 20, 21);
        model.add(WorldModelA.GOLD, 20, 22);
        model.add(WorldModelA.GOLD, 20, 23);
        model.add(WorldModelA.GOLD, 20, 24);
        model.add(WorldModelA.GOLD, 19, 20);
        model.add(WorldModelA.GOLD, 19, 21);
        model.add(WorldModelA.GOLD, 34, 34);
        model.setInitialNbGolds(model.countObjects(WorldModelA.GOLD));
        return model;
    }



    /** world with gold, some obstacle */
    static WorldModelA world5() throws Exception {
        WorldModelA model = WorldModelA.create(35, 35, 4);
        model.setId("Scenario 5");
        model.setDepot(5, 27);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 20, 0);
        model.setAgPos(2, 3, 20);
        model.setAgPos(3, 20, 20);
        model.add(WorldModelA.GOLD, 20, 13);
        model.add(WorldModelA.GOLD, 15, 20);
        model.add(WorldModelA.GOLD, 1, 1);
        model.add(WorldModelA.GOLD, 3, 5);
        model.add(WorldModelA.GOLD, 24, 24);
        model.add(WorldModelA.GOLD, 20, 20);
        model.add(WorldModelA.GOLD, 20, 21);
        model.add(WorldModelA.GOLD, 20, 22);
        model.add(WorldModelA.GOLD, 20, 23);
        model.add(WorldModelA.GOLD, 20, 24);
        model.add(WorldModelA.GOLD, 19, 20);
        model.add(WorldModelA.GOLD, 19, 21);
        model.add(WorldModelA.GOLD, 34, 34);

        model.add(WorldModelA.OBSTACLE, 12, 3);
        model.add(WorldModelA.OBSTACLE, 13, 3);
        model.add(WorldModelA.OBSTACLE, 14, 3);
        model.add(WorldModelA.OBSTACLE, 15, 3);
        model.add(WorldModelA.OBSTACLE, 18, 3);
        model.add(WorldModelA.OBSTACLE, 19, 3);
        model.add(WorldModelA.OBSTACLE, 20, 3);
        model.add(WorldModelA.OBSTACLE, 14, 8);
        model.add(WorldModelA.OBSTACLE, 15, 8);
        model.add(WorldModelA.OBSTACLE, 16, 8);
        model.add(WorldModelA.OBSTACLE, 17, 8);
        model.add(WorldModelA.OBSTACLE, 19, 8);
        model.add(WorldModelA.OBSTACLE, 20, 8);
        model.setInitialNbGolds(model.countObjects(WorldModelA.GOLD));
        return model;
    }


    /** world with gold and obstacles */
    static WorldModelA world6() throws Exception {
        WorldModelA model = WorldModelA.create(35, 35, 4);
        model.setId("Scenario 6");
        model.setDepot(16, 16);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 20, 0);
        model.setAgPos(2, 6, 26);
        model.setAgPos(3, 20, 20);
        model.add(WorldModelA.GOLD, 20, 13);
        model.add(WorldModelA.GOLD, 15, 20);
        model.add(WorldModelA.GOLD, 1, 1);
        model.add(WorldModelA.GOLD, 3, 5);
        model.add(WorldModelA.GOLD, 24, 24);
        model.add(WorldModelA.GOLD, 20, 20);
        model.add(WorldModelA.GOLD, 20, 21);
        model.add(WorldModelA.GOLD, 2, 22);
        model.add(WorldModelA.GOLD, 2, 12);
        model.add(WorldModelA.GOLD, 19, 2);
        model.add(WorldModelA.GOLD, 14, 4);
        model.add(WorldModelA.GOLD, 34, 34);

        model.add(WorldModelA.OBSTACLE, 12, 3);
        model.add(WorldModelA.OBSTACLE, 13, 3);
        model.add(WorldModelA.OBSTACLE, 14, 3);
        model.add(WorldModelA.OBSTACLE, 15, 3);
        model.add(WorldModelA.OBSTACLE, 18, 3);
        model.add(WorldModelA.OBSTACLE, 19, 3);
        model.add(WorldModelA.OBSTACLE, 20, 3);
        model.add(WorldModelA.OBSTACLE, 14, 8);
        model.add(WorldModelA.OBSTACLE, 15, 8);
        model.add(WorldModelA.OBSTACLE, 16, 8);
        model.add(WorldModelA.OBSTACLE, 17, 8);
        model.add(WorldModelA.OBSTACLE, 19, 8);
        model.add(WorldModelA.OBSTACLE, 20, 8);

        model.add(WorldModelA.OBSTACLE, 12, 32);
        model.add(WorldModelA.OBSTACLE, 13, 32);
        model.add(WorldModelA.OBSTACLE, 14, 32);
        model.add(WorldModelA.OBSTACLE, 15, 32);
        model.add(WorldModelA.OBSTACLE, 18, 32);
        model.add(WorldModelA.OBSTACLE, 19, 32);
        model.add(WorldModelA.OBSTACLE, 20, 32);
        model.add(WorldModelA.OBSTACLE, 14, 28);
        model.add(WorldModelA.OBSTACLE, 15, 28);
        model.add(WorldModelA.OBSTACLE, 16, 28);
        model.add(WorldModelA.OBSTACLE, 17, 28);
        model.add(WorldModelA.OBSTACLE, 19, 28);
        model.add(WorldModelA.OBSTACLE, 20, 28);

        model.add(WorldModelA.OBSTACLE, 3, 12);
        model.add(WorldModelA.OBSTACLE, 3, 13);
        model.add(WorldModelA.OBSTACLE, 3, 14);
        model.add(WorldModelA.OBSTACLE, 3, 15);
        model.add(WorldModelA.OBSTACLE, 3, 18);
        model.add(WorldModelA.OBSTACLE, 3, 19);
        model.add(WorldModelA.OBSTACLE, 3, 20);
        model.add(WorldModelA.OBSTACLE, 8, 14);
        model.add(WorldModelA.OBSTACLE, 8, 15);
        model.add(WorldModelA.OBSTACLE, 8, 16);
        model.add(WorldModelA.OBSTACLE, 8, 17);
        model.add(WorldModelA.OBSTACLE, 8, 19);
        model.add(WorldModelA.OBSTACLE, 8, 20);

        model.add(WorldModelA.OBSTACLE, 32, 12);
        model.add(WorldModelA.OBSTACLE, 32, 13);
        model.add(WorldModelA.OBSTACLE, 32, 14);
        model.add(WorldModelA.OBSTACLE, 32, 15);
        model.add(WorldModelA.OBSTACLE, 32, 18);
        model.add(WorldModelA.OBSTACLE, 32, 19);
        model.add(WorldModelA.OBSTACLE, 32, 20);
        model.add(WorldModelA.OBSTACLE, 28, 14);
        model.add(WorldModelA.OBSTACLE, 28, 15);
        model.add(WorldModelA.OBSTACLE, 28, 16);
        model.add(WorldModelA.OBSTACLE, 28, 17);
        model.add(WorldModelA.OBSTACLE, 28, 19);
        model.add(WorldModelA.OBSTACLE, 28, 20);

        model.add(WorldModelA.OBSTACLE, 13, 13);
        model.add(WorldModelA.OBSTACLE, 13, 14);

        model.add(WorldModelA.OBSTACLE, 13, 16);
        model.add(WorldModelA.OBSTACLE, 13, 17);

        model.add(WorldModelA.OBSTACLE, 13, 19);
        model.add(WorldModelA.OBSTACLE, 14, 19);

        model.add(WorldModelA.OBSTACLE, 16, 19);
        model.add(WorldModelA.OBSTACLE, 17, 19);

        model.add(WorldModelA.OBSTACLE, 19, 19);
        model.add(WorldModelA.OBSTACLE, 19, 18);

        model.add(WorldModelA.OBSTACLE, 19, 16);
        model.add(WorldModelA.OBSTACLE, 19, 15);

        model.add(WorldModelA.OBSTACLE, 19, 13);
        model.add(WorldModelA.OBSTACLE, 18, 13);

        model.add(WorldModelA.OBSTACLE, 16, 13);
        model.add(WorldModelA.OBSTACLE, 15, 13);

        // labirinto
        model.add(WorldModelA.OBSTACLE, 2, 32);
        model.add(WorldModelA.OBSTACLE, 3, 32);
        model.add(WorldModelA.OBSTACLE, 4, 32);
        model.add(WorldModelA.OBSTACLE, 5, 32);
        model.add(WorldModelA.OBSTACLE, 6, 32);
        model.add(WorldModelA.OBSTACLE, 7, 32);
        model.add(WorldModelA.OBSTACLE, 8, 32);
        model.add(WorldModelA.OBSTACLE, 9, 32);
        model.add(WorldModelA.OBSTACLE, 10, 32);
        model.add(WorldModelA.OBSTACLE, 10, 31);
        model.add(WorldModelA.OBSTACLE, 10, 30);
        model.add(WorldModelA.OBSTACLE, 10, 29);
        model.add(WorldModelA.OBSTACLE, 10, 28);
        model.add(WorldModelA.OBSTACLE, 10, 27);
        model.add(WorldModelA.OBSTACLE, 10, 26);
        model.add(WorldModelA.OBSTACLE, 10, 25);
        model.add(WorldModelA.OBSTACLE, 10, 24);
        model.add(WorldModelA.OBSTACLE, 10, 23);
        model.add(WorldModelA.OBSTACLE, 2, 23);
        model.add(WorldModelA.OBSTACLE, 3, 23);
        model.add(WorldModelA.OBSTACLE, 4, 23);
        model.add(WorldModelA.OBSTACLE, 5, 23);
        model.add(WorldModelA.OBSTACLE, 6, 23);
        model.add(WorldModelA.OBSTACLE, 7, 23);
        model.add(WorldModelA.OBSTACLE, 8, 23);
        model.add(WorldModelA.OBSTACLE, 9, 23);
        model.add(WorldModelA.OBSTACLE, 2, 29);
        model.add(WorldModelA.OBSTACLE, 2, 28);
        model.add(WorldModelA.OBSTACLE, 2, 27);
        model.add(WorldModelA.OBSTACLE, 2, 26);
        model.add(WorldModelA.OBSTACLE, 2, 25);
        model.add(WorldModelA.OBSTACLE, 2, 24);
        model.add(WorldModelA.OBSTACLE, 2, 23);
        model.add(WorldModelA.OBSTACLE, 2, 29);
        model.add(WorldModelA.OBSTACLE, 3, 29);
        model.add(WorldModelA.OBSTACLE, 4, 29);
        model.add(WorldModelA.OBSTACLE, 5, 29);
        model.add(WorldModelA.OBSTACLE, 6, 29);
        model.add(WorldModelA.OBSTACLE, 7, 29);
        model.add(WorldModelA.OBSTACLE, 7, 28);
        model.add(WorldModelA.OBSTACLE, 7, 27);
        model.add(WorldModelA.OBSTACLE, 7, 26);
        model.add(WorldModelA.OBSTACLE, 7, 25);
        model.add(WorldModelA.OBSTACLE, 6, 25);
        model.add(WorldModelA.OBSTACLE, 5, 25);
        model.add(WorldModelA.OBSTACLE, 4, 25);
        model.add(WorldModelA.OBSTACLE, 4, 26);
        model.add(WorldModelA.OBSTACLE, 4, 27);
        model.setInitialNbGolds(model.countObjects(WorldModelA.GOLD));
        return model;
    }

}
