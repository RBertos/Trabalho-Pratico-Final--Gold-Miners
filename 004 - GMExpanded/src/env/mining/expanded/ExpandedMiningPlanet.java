package mining.expanded;

import java.awt.GraphicsEnvironment;
import java.util.logging.Logger;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.ObsProperty;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

public class ExpandedMiningPlanet extends Artifact {
    private static final Logger logger = Logger.getLogger(ExpandedMiningPlanet.class.getName());
    private static final Term GOLD_TERM = new Atom("gold");
    private static final Term OBSTACLE_TERM = new Atom("obstacle");
    private static final Term DEPOT_TERM = new Atom("depot");

    private static ExpandedWorldModel model;
    private static ExpandedWorldView view;
    private static int simId = 6;
    private static int sleep = Config.GUI_SLEEP_MS;
    private static boolean hasGUI = true;

    private int agId = -1;

    public enum Move {
        UP, DOWN, RIGHT, LEFT
    }

    @OPERATION
    public void init(int scenario, int agId) {
        this.agId = agId;
        initWorld(scenario);
    }

    public void setSleep(int s) {
        sleep = s;
    }

    @OPERATION
    public void up() throws Exception {
        move(Move.UP);
    }

    @OPERATION
    public void down() throws Exception {
        move(Move.DOWN);
    }

    @OPERATION
    public void right() throws Exception {
        move(Move.RIGHT);
    }

    @OPERATION
    public void left() throws Exception {
        move(Move.LEFT);
    }

    @OPERATION
    public void pick() throws Exception {
        delay();
        model.pick(agId);
        refresh();
    }

    @OPERATION
    public void drop() throws Exception {
        delay();
        model.drop(agId);
        refresh();
    }

    @OPERATION
    public void skip() throws Exception {
        delay();
        model.skip(agId);
        refresh();
    }

    @OPERATION
    public void comm_tick(String label) throws Exception {
        delay();
        model.commTick(agId, label);
        refresh();
    }

    @OPERATION
    public void equip(String item) throws Exception {
        delay();
        model.equip(agId, EquipmentType.fromAtom(item));
        refresh();
    }

    @OPERATION
    public void unequip(String item) throws Exception {
        delay();
        model.unequip(agId, EquipmentType.fromAtom(item));
        refresh();
    }

    @OPERATION
    public void mission(String mission) throws Exception {
        model.setMission(agId, mission);
        refresh();
    }

    public synchronized void initWorld(int scenario) {
        simId = scenario;
        try {
            if (model == null) {
                model = ExpandedWorldModel.scenario(scenario);
                if (hasGUI && !GraphicsEnvironment.isHeadless()) {
                    try {
                        view = new ExpandedWorldView(model);
                        view.setEnv(this);
                        view.updateStatus();
                    } catch (RuntimeException e) {
                        hasGUI = false;
                        logger.info("GUI disabled: " + e.getMessage());
                    }
                } else if (hasGUI) {
                    hasGUI = false;
                    logger.info("GUI disabled: headless Java environment");
                }
            }
            defineObsProperty("gsize", simId, model.getWidth(), model.getHeight());
            defineObsProperty("depot", simId, model.getDepot().x, model.getDepot().y);
            defineObsProperty("pos", -1, -1);
            defineObsProperty("tick", 0);
            defineObsProperty("cargo", 0, model.goldCapacity(agId));
            defineObsProperty("vision_radius", model.viewRadius(agId));
            defineObsProperty("inventory_slots", 0, Config.BASE_EQUIPMENT_SLOTS);
            updateAgPercept();
        } catch (Exception e) {
            logger.warning("Error creating expanded world " + e);
            e.printStackTrace();
        }
    }

    public void endSimulation() {
        defineObsProperty("end_of_simulation", simId, 0);
        if (view != null) {
            view.setVisible(false);
        }
        ExpandedWorldModel.destroy();
    }

    private void move(Move move) throws Exception {
        delay();
        model.move(move, agId);
        refresh();
    }

    private void delay() throws Exception {
        if (sleep > 0) {
            await_time(sleep);
        }
    }

    private void refresh() {
        updateAgPercept();
        if (view != null) {
            view.updateStatus();
        }
    }

    private void updateAgPercept() {
        Location location = model.getAgPos(agId);
        updateProperty("pos", location.x, location.y);
        updateProperty("tick", (int) model.tick());
        updateProperty("cargo", model.carryingGold(agId), model.goldCapacity(agId));
        updateProperty("vision_radius", model.viewRadius(agId));
        updateProperty("inventory_slots", Config.BASE_EQUIPMENT_SLOTS - model.agent(agId).inventory().freeSlots(),
                Config.BASE_EQUIPMENT_SLOTS);

        resetProperty("carrying_gold");
        if (model.isCarryingGold(agId)) {
            defineObsProperty("carrying_gold");
        }

        resetTemplate("equipped", (Object) null);
        for (EquipmentType type : EquipmentType.values()) {
            if (model.hasEquipment(agId, type)) {
                defineObsProperty("equipped", new Atom(type.atom()));
            }
        }

        resetTemplate("base_stock", null, null);
        for (EquipmentType type : EquipmentType.values()) {
            defineObsProperty("base_stock", new Atom(type.atom()), model.baseStock(type));
        }

        resetTemplate("cell", null, null, null);
        int radius = model.viewRadius(agId);
        for (int x = location.x - radius; x <= location.x + radius; x++) {
            for (int y = location.y - radius; y <= location.y + radius; y++) {
                updateCellPercept(x, y);
            }
        }
    }

    private void updateCellPercept(int x, int y) {
        if (model == null || !model.inGrid(x, y)) {
            return;
        }
        if (model.hasObject(ExpandedWorldModel.OBSTACLE, x, y)) {
            defineObsProperty("cell", x, y, OBSTACLE_TERM);
        } else if (model.hasObject(ExpandedWorldModel.GOLD, x, y)) {
            defineObsProperty("cell", x, y, GOLD_TERM);
        } else if (model.hasObject(ExpandedWorldModel.DEPOT, x, y)) {
            defineObsProperty("cell", x, y, DEPOT_TERM);
        }
    }

    private void updateProperty(String name, Object... values) {
        ObsProperty property = getObsProperty(name);
        if (property == null) {
            defineObsProperty(name, values);
            return;
        }
        for (int i = 0; i < values.length; i++) {
            property.updateValue(i, values[i]);
        }
    }

    private void resetProperty(String name) {
        try {
            while (hasObsProperty(name)) {
                removeObsProperty(name);
            }
        } catch (IllegalArgumentException e) {
            // Property was already absent.
        }
    }

    private void resetTemplate(String name, Object... values) {
        // removeObsPropertyByTemplate removes one match at a time,
        // so loop until there are no properties left for this template.
        while (true) {
            try {
                removeObsPropertyByTemplate(name, values);
            } catch (IllegalArgumentException e) {
                break;
            }
        }
    }
}
