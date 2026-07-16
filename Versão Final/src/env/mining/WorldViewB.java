package mining;

import jason.environment.grid.GridWorldView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
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
public class WorldViewB extends GridWorldView {

    MiningPlanetB env = null;

    public WorldViewB(WorldModelB model) {
        super(model, "Mining World", 600);
        setVisible(true);
        repaint();
    }

    public void setEnv(MiningPlanetB env) {
        this.env = env;
        //scenarios.setSelectedIndex(env.getSimId()-1);
    }

    JLabel    jlMouseLoc;
    //JComboBox scenarios;
    JSlider   jSpeed;
    JLabel    jGoldsC;

    @Override
    public void initComponents(int width) {
        super.initComponents(width);
        JPanel args = new JPanel();
        args.setLayout(new BoxLayout(args, BoxLayout.Y_AXIS));
        /*scenarios = new JComboBox();
        for (int i=1; i<=6; i++) {
            scenarios.addItem(i);
        }

        JPanel sp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sp.setBorder(BorderFactory.createEtchedBorder());
        sp.add(new JLabel("Scenario:"));
        sp.add(scenarios);*/

        jSpeed = new JSlider();
        jSpeed.setMinimum(0);
        jSpeed.setMaximum(400);
        jSpeed.setValue(50);
        jSpeed.setPaintTicks(true);
        jSpeed.setPaintLabels(true);
        jSpeed.setMajorTickSpacing(100);
        jSpeed.setMinorTickSpacing(20);
        jSpeed.setInverted(true);
        Hashtable<Integer,Component> labelTable = new Hashtable<Integer,Component>();
        labelTable.put( 0, new JLabel("max") );
        labelTable.put( 200, new JLabel("speed") );
        labelTable.put( 400, new JLabel("min") );
        jSpeed.setLabelTable( labelTable );
        JPanel p = new JPanel(new FlowLayout());
        p.setBorder(BorderFactory.createEtchedBorder());
        p.add(jSpeed);

        //args.add(sp);
        args.add(p);

        JPanel msg = new JPanel();
        msg.setLayout(new BoxLayout(msg, BoxLayout.Y_AXIS));
        msg.setBorder(BorderFactory.createEtchedBorder());

        p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.add(new JLabel("Click on the cells to add new pieces of gold."));
        msg.add(p);
        p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.add(new JLabel("(mouse at:"));
        jlMouseLoc = new JLabel("0,0)");
        p.add(jlMouseLoc);
        msg.add(p);
        p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.add(new JLabel("Collected ores:"));
        jGoldsC = new JLabel("0");
        p.add(jGoldsC);
        msg.add(p);

        JPanel s = new JPanel(new BorderLayout());
        s.add(BorderLayout.WEST, args);
        s.add(BorderLayout.CENTER, msg);
        getContentPane().add(BorderLayout.SOUTH, s);

        // Events handling
        jSpeed.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (env != null) {
                    env.setSleep((int)jSpeed.getValue());
                }
            }
        });

        /*scenarios.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ievt) {
                int w = ((Integer)scenarios.getSelectedItem()).intValue();
                if (env != null && env.getSimId() != w) {
                    env.endSimulation();
                    env.initWorld(w);
                }
            }
        });*/

        getCanvas().addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                int col = e.getX() / cellSizeW;
                int lin = e.getY() / cellSizeH;
                if (col >= 0 && lin >= 0 && col < getModel().getWidth() && lin < getModel().getHeight()) {
                    WorldModelB wm = (WorldModelB)model;
                    wm.add(WorldModelB.GOLD, col, lin);
                    wm.setInitialNbGolds(wm.getInitialNbGolds()+1);
                    update(col, lin);
                    udpateCollectedGolds();
                }
            }
            public void mouseExited(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
        });

        getCanvas().addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) { }
            public void mouseMoved(MouseEvent e) {
                int col = e.getX() / cellSizeW;
                int lin = e.getY() / cellSizeH;
                if (col >= 0 && lin >= 0 && col < getModel().getWidth() && lin < getModel().getHeight()) {
                    jlMouseLoc.setText(col+","+lin+")");
                }
            }
        });
    }

    public void udpateCollectedGolds() {
        WorldModelB wm = (WorldModelB)model;
        jGoldsC.setText(wm.getGoldsInDepot() + "/" + wm.getInitialNbGolds());
    }

    @Override
    public void draw(Graphics g, int x, int y, int object) {
        switch (object) {
        case WorldModelB.DEPOT:   drawDepot(g, x, y);  break;
        case WorldModelB.COAL:    drawOre(g, x, y, Color.darkGray); break;
        case WorldModelB.IRON:    drawOre(g, x, y, Color.lightGray); break;
        case WorldModelB.GOLD:    drawOre(g, x, y, Color.yellow); break;
        case WorldModelB.DIAMOND: drawOre(g, x, y, Color.cyan); break;
        case WorldModelB.ENEMY:   drawEnemy(g, x, y);  break;
        }
    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        WorldModelB wm = (WorldModelB)model;
        Color idColor = Color.white;
        super.drawAgent(g, x, y, teamColor(id), -1);
        g.setColor(idColor);
        int load = wm.getBackpackLoad(id);
        String label = load > 0 ? String.valueOf(id + 1) + "/" + load : String.valueOf(id + 1);
        drawString(g, x, y, defaultFont, label);
    }

    private Color teamColor(int id) {
        if (id >= 0 && id <= 2) return Color.red;
        if (id >= 3 && id <= 5) return Color.blue;
        if (id >= 6 && id <= 8) return new Color(0, 150, 0);
        return Color.gray;
    }

    private Color oreColor(int ore) {
        switch (ore) {
        case WorldModelB.COAL:    return Color.darkGray;
        case WorldModelB.IRON:    return Color.lightGray;
        case WorldModelB.GOLD:    return Color.yellow;
        case WorldModelB.DIAMOND: return Color.cyan;
        default:                  return Color.yellow;
        }
    }

    public void drawDepot(Graphics g, int x, int y) {
        g.setColor(Color.gray);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        g.setColor(Color.pink);
        g.drawRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
        g.drawLine(x * cellSizeW + 2, y * cellSizeH + 2, (x + 1) * cellSizeW - 2, (y + 1) * cellSizeH - 2);
        g.drawLine(x * cellSizeW + 2, (y + 1) * cellSizeH - 2, (x + 1) * cellSizeW - 2, y * cellSizeH + 2);
    }

    public void drawOre(Graphics g, int x, int y, Color color) {
        g.setColor(color);
        g.drawRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);

        int[] vx = new int[4];
        int[] vy = new int[4];

        vx[0] = x * cellSizeW + (cellSizeW / 2);
        vy[0] = y * cellSizeH;
        vx[1] = (x + 1) * cellSizeW;
        vy[1] = y * cellSizeH + (cellSizeH / 2);
        vx[2] = x * cellSizeW + (cellSizeW / 2);
        vy[2] = (y + 1) * cellSizeH;
        vx[3] = x * cellSizeW;
        vy[3] = y * cellSizeH + (cellSizeH / 2);

        g.fillPolygon(vx, vy, 4);
    }

    public void drawEnemy(Graphics g, int x, int y) {
        g.setColor(Color.red);
        g.fillOval(x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8);
    }

}
