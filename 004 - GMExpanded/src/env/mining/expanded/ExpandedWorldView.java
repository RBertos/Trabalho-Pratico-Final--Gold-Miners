package mining.expanded;

import jason.environment.grid.GridWorldView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class ExpandedWorldView extends GridWorldView {
    private ExpandedMiningPlanet env;
    private JLabel mouseLoc;
    private JLabel golds;
    private JLabel tick;
    private JLabel baseStock;
    private JLabel lastEvent;
    private JLabel[] agents;
    private JSlider speed;

    public ExpandedWorldView(ExpandedWorldModel model) {
        super(model, "Expanded Gold Miners", 720);
        setVisible(true);
        repaint();
    }

    public void setEnv(ExpandedMiningPlanet env) {
        this.env = env;
    }

    @Override
    public void initComponents(int width) {
        super.initComponents(width);

        JPanel root = new JPanel(new BorderLayout());

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBorder(BorderFactory.createEtchedBorder());

        speed = new JSlider(0, 300, Config.GUI_SLEEP_MS);
        speed.setPaintTicks(true);
        speed.setPaintLabels(true);
        speed.setMajorTickSpacing(100);
        speed.setMinorTickSpacing(25);
        speed.setInverted(true);
        Hashtable<Integer, Component> labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel("max"));
        labelTable.put(150, new JLabel("speed"));
        labelTable.put(300, new JLabel("min"));
        speed.setLabelTable(labelTable);
        controls.add(speed);

        JPanel status = new JPanel(new GridLayout(0, 1));
        status.setBorder(BorderFactory.createEtchedBorder());
        tick = new JLabel("Tick: 0");
        golds = new JLabel("Gold: 0/0");
        baseStock = new JLabel("Base: -");
        lastEvent = new JLabel("Event: -");
        mouseLoc = new JLabel("Mouse: 0,0");
        status.add(tick);
        status.add(golds);
        status.add(baseStock);
        status.add(lastEvent);
        status.add(mouseLoc);

        JPanel agentPanel = new JPanel(new GridLayout(0, 1));
        agentPanel.setBorder(BorderFactory.createEtchedBorder());
        agents = new JLabel[Config.NB_AGENTS];
        for (int i = 0; i < Config.NB_AGENTS; i++) {
            agents[i] = new JLabel("miner" + (i + 1));
            agentPanel.add(agents[i]);
        }

        root.add(controls, BorderLayout.WEST);
        root.add(status, BorderLayout.CENTER);
        root.add(agentPanel, BorderLayout.EAST);
        getContentPane().add(BorderLayout.SOUTH, root);

        speed.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (env != null) {
                    env.setSleep(speed.getValue());
                }
            }
        });

        getCanvas().addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                int col = e.getX() / cellSizeW;
                int row = e.getY() / cellSizeH;
                ExpandedWorldModel wm = (ExpandedWorldModel) model;
                if (col >= 0 && row >= 0 && col < wm.getWidth() && row < wm.getHeight()) {
                    wm.addGold(col, row);
                    update(col, row);
                    updateStatus();
                }
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }
        });

        getCanvas().addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
            }

            public void mouseMoved(MouseEvent e) {
                int col = e.getX() / cellSizeW;
                int row = e.getY() / cellSizeH;
                ExpandedWorldModel wm = (ExpandedWorldModel) model;
                if (col >= 0 && row >= 0 && col < wm.getWidth() && row < wm.getHeight()) {
                    mouseLoc.setText("Mouse: " + col + "," + row);
                }
            }
        });
    }

    public void updateStatus() {
        ExpandedWorldModel wm = (ExpandedWorldModel) model;
        tick.setText("Tick: " + wm.tick());
        golds.setText("Gold: " + wm.getGoldsInDepot() + "/" + wm.getInitialNbGolds());
        baseStock.setText("Base: " + wm.baseEquipmentSummary());
        lastEvent.setText("Event: " + wm.lastEvent());
        for (int i = 0; i < agents.length; i++) {
            agents[i].setText("miner" + (i + 1)
                    + " cargo " + wm.carryingGold(i) + "/" + wm.goldCapacity(i)
                    + " eq [" + wm.equipmentSummary(i) + "]"
                    + " score " + wm.depositedBy(i));
        }
        repaint();
    }

    @Override
    public void draw(Graphics g, int x, int y, int object) {
        switch (object) {
            case ExpandedWorldModel.DEPOT:
                drawDepot(g, x, y);
                break;
            case ExpandedWorldModel.GOLD:
                drawGold(g, x, y);
                break;
            case ExpandedWorldModel.ENEMY:
                drawEnemy(g, x, y);
                break;
            default:
                break;
        }
    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        ExpandedWorldModel wm = (ExpandedWorldModel) model;
        if (wm.isCarryingGold(id)) {
            super.drawAgent(g, x, y, Color.yellow, -1);
        } else if (wm.hasEquipment(id, EquipmentType.CART)) {
            super.drawAgent(g, x, y, new Color(108, 92, 231), -1);
        } else if (wm.hasEquipment(id, EquipmentType.LANTERN)) {
            super.drawAgent(g, x, y, new Color(245, 166, 35), -1);
        } else if (wm.hasEquipment(id, EquipmentType.BACKPACK)) {
            super.drawAgent(g, x, y, new Color(46, 160, 67), -1);
        } else {
            super.drawAgent(g, x, y, c, -1);
        }
        g.setColor(Color.black);
        drawString(g, x, y, defaultFont, String.valueOf(id + 1));

        if (wm.hasEquipment(id, EquipmentType.CART)) {
            g.setColor(Color.darkGray);
            g.drawRect(x * cellSizeW + 3, y * cellSizeH + cellSizeH - 6, cellSizeW - 6, 3);
        }
    }

    private void drawDepot(Graphics g, int x, int y) {
        g.setColor(Color.gray);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        g.setColor(Color.white);
        g.drawRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
        g.drawString("B", x * cellSizeW + cellSizeW / 3, y * cellSizeH + (2 * cellSizeH) / 3);
    }

    private void drawGold(Graphics g, int x, int y) {
        g.setColor(Color.yellow);
        int[] vx = new int[4];
        int[] vy = new int[4];
        vx[0] = x * cellSizeW + (cellSizeW / 2);
        vy[0] = y * cellSizeH + 2;
        vx[1] = (x + 1) * cellSizeW - 2;
        vy[1] = y * cellSizeH + (cellSizeH / 2);
        vx[2] = x * cellSizeW + (cellSizeW / 2);
        vy[2] = (y + 1) * cellSizeH - 2;
        vx[3] = x * cellSizeW + 2;
        vy[3] = y * cellSizeH + (cellSizeH / 2);
        g.fillPolygon(vx, vy, 4);
        g.setColor(Color.orange.darker());
        g.drawPolygon(vx, vy, 4);
    }

    private void drawEnemy(Graphics g, int x, int y) {
        g.setColor(Color.red);
        g.fillOval(x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8);
    }
}
