package mining;

import jason.asSyntax.Atom;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

import java.util.logging.Logger;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.ObsProperty;

public class MiningPlanetB extends Artifact {

    private static Logger logger = Logger.getLogger(MiningPlanetB.class.getName());

    static WorldModelB  model = null;
    static WorldViewB   view;

    static int     simId    = 5; // type of environment
    static int     sleep    = 200;
    static boolean hasGUI   = true;

    int     agId     = -1;

    public enum Move {
        UP, DOWN, RIGHT, LEFT
    };

    @OPERATION
    public void init(int scenario, int agId) {
        this.agId = agId;
        initWorld(scenario);
    }

    public int getSimId() {
        return simId;
    }

    public void setSleep(int s) {
        sleep = s;
    }

    @OPERATION void up() throws Exception {     move(Move.UP);    }
    @OPERATION void down() throws Exception {   move(Move.DOWN);  }
    @OPERATION void right() throws Exception {  move(Move.RIGHT); }
    @OPERATION void left() throws Exception {   move(Move.LEFT);  }
    void move(Move m) throws Exception {
        if (sleep > 0) await_time(sleep);
        model.move(m, agId);
        updateAgPercept();
    }

    @OPERATION void pick() throws Exception {
        if (sleep > 0) await_time(sleep);
        model.pick(agId);
        updateAgPercept();
    }
    @OPERATION void drop() throws Exception {
        if (sleep > 0) await_time(sleep);
        model.drop(agId);
        view.udpateCollectedGolds();
        updateAgPercept();
    }
    @OPERATION void skip() {
        if (sleep > 0) await_time(sleep);
        updateAgPercept();
    }

    public synchronized void initWorld(int w) {
        simId = w;
        try {
            if (model == null) {
                switch (w) {
                case 1: model = WorldModelB.world1(); break;
                case 2: model = WorldModelB.world2(); break;
                case 3: model = WorldModelB.world3(); break;
                case 4: model = WorldModelB.world4(); break;
                case 5: model = WorldModelB.world5(); break;
                case 6: model = WorldModelB.world6(); break;
                default:
                    logger.info("Invalid index!");
                    return;
                }
                if (hasGUI) {
                    view = new WorldViewB(model);
                    view.setEnv(this);
                    view.udpateCollectedGolds();
                }
            }
            defineObsProperty("gsize", simId, model.getWidth(), model.getHeight());
            defineObsProperty("depot", simId, model.getDepot().x, model.getDepot().y);
            defineObsProperty("pos", -1, -1);
            defineObsProperty("backpack_capacity", 1);
            defineObsProperty("backpack_load", 0);
            defineObsProperty("backpack_value", 0);
            defineObsProperty("last_pick", 0);
            updateAgPercept();
            //informAgsEnvironmentChanged();
        } catch (Exception e) {
            logger.warning("Error creating world "+e);
            e.printStackTrace();
        }
    }

    public void endSimulation() {
        defineObsProperty("end_of_simulation", simId, 0);
        //informAgsEnvironmentChanged();
        if (view != null) view.setVisible(false);
        WorldModelB.destroy();
    }

    private void updateAgPercept() {
        Location l = model.getAgPos(agId);

        ObsProperty p = getObsProperty("pos");
        p.updateValue(0, l.x);
        p.updateValue(1, l.y);

        getObsProperty("backpack_capacity").updateValue(0, model.getBackpackCapacity(agId));
        getObsProperty("backpack_load").updateValue(0, model.getBackpackLoad(agId));
        getObsProperty("backpack_value").updateValue(0, model.getBackpackValue(agId));
        getObsProperty("last_pick").updateValue(0, model.getLastPickedOre(agId));

        if (model.isCarryingGold(agId)) {
            if (!hasObsProperty("carrying_gold"))
                defineObsProperty("carrying_gold");
        } else try {
            removeObsProperty("carrying_gold");
        } catch (IllegalArgumentException e) {}

        try {
            removeObsPropertyByTemplate("cell", null, null, null, null);
        } catch (IllegalArgumentException e) {}

        int visionRadius = LanternArtifact.hasLantern(agId) ? 2 : 1;
        for (int dx = -visionRadius; dx <= visionRadius; dx++) {
            for (int dy = -visionRadius; dy <= visionRadius; dy++) {
                updateCellPercept(l.x + dx, l.y + dy);
            }
        }
    }

    private static Term coal     = new Atom("coal");
    private static Term iron     = new Atom("iron");
    private static Term gold     = new Atom("gold");
    private static Term diamond  = new Atom("diamond");
    private static Term obstacle = new Atom("obstacle");

    private void updateCellPercept(int x, int y) {
        if (model == null || !model.inGrid(x,y)) return;

        if (model.hasObject(WorldModelB.OBSTACLE, x, y)) {
            defineObsProperty("cell", x, y, obstacle, 0);
        } else if (model.hasObject(WorldModelB.COAL, x, y)) {
            defineObsProperty("cell", x, y, coal, 1);
        } else if (model.hasObject(WorldModelB.IRON, x, y)) {
            defineObsProperty("cell", x, y, iron, 2);
        } else if (model.hasObject(WorldModelB.GOLD, x, y)) {
            defineObsProperty("cell", x, y, gold, 3);
        } else if (model.hasObject(WorldModelB.DIAMOND, x, y)) {
            defineObsProperty("cell", x, y, diamond, 4);
        }
    }
}
