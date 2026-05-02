import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GraphPanel extends JPanel {

    private JLabel statusLabel;

    private TrafficGraph graph;

    private int nodeCounter = 0;

    private Intersection selected = null;

    private Intersection startNode = null;
    private Intersection endNode = null;

    private boolean avoidTolls = false;
    private boolean createTollRoad = false;

    private int currentHour = 8; // 24-hour clock (0–23)

    private java.util.List<Intersection> shortestPath = new java.util.ArrayList<>();

    private enum Mode {
        BUILD,
        PATH
    }

    private Mode mode = Mode.BUILD;

    public GraphPanel(TrafficGraph graph, JLabel statusLabel) {
        this.graph = graph;
        this.statusLabel = statusLabel;

        setBackground(Color.LIGHT_GRAY);


        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {

                char key = e.getKeyChar();
                int code = e.getKeyCode();

                // mode toggle
                if (key == 'm') {
                    mode = (mode == Mode.BUILD) ? Mode.PATH : Mode.BUILD;
                    updateStatus();
                }

                // avoid tolls
                else if (key == 't') {
                    avoidTolls = !avoidTolls;
                    updateStatus();
                }

                // creating toll roads?
                else if (key == 'p') {
                    createTollRoad = !createTollRoad;
                    updateStatus();
                }

               //controlling time
                else if (code == java.awt.event.KeyEvent.VK_UP) {
                    currentHour = (currentHour + 1) % 24;
                    updateStatus();
                    repaint();
                }

                else if (code == java.awt.event.KeyEvent.VK_DOWN) {
                    currentHour = (currentHour - 1 + 24) % 24;
                    updateStatus();
                    repaint();
                }
            }
        });

        setFocusable(true);
        requestFocusInWindow();

        //mouse inputs
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                int x = e.getX();
                int y = e.getY();

                Intersection clicked = getClickedIntersection(x, y);

                //new node
                if (clicked == null) {

                    if (mode == Mode.PATH) return;

                    String id = "I" + nodeCounter++;
                    Intersection newNode = new Intersection(id, x, y);
                    graph.addIntersection(newNode);

                    selected = null;
                    repaint();
                    return;
                }

                //switching in path modes
                if (mode == Mode.PATH) {

                    if (startNode == null) {
                        startNode = clicked;
                    } else {
                        endNode = clicked;

                        RouteOptimizer.getShortestPath(
                                graph,
                                startNode,
                                endNode,
                                avoidTolls,
                                currentHour
                        );

                        startNode = null;
                        endNode = null;
                    }
                }

                //build modes.
                else {

                    if (selected == null) {
                        selected = clicked;
                    } else {

                        if (selected != clicked && !roadExists(selected, clicked)) {

                            double distance = calculateDistance(selected, clicked);

                            Road r = new Road(selected, clicked, distance);

                            if (createTollRoad) {
                                r.tollCost = 3.0;
                            }

                            graph.addRoad(r);
                        }

                        selected = null;
                    }
                }

                repaint();
            }
        });

        updateStatus();
    }

    //distance
    private double calculateDistance(Intersection a, Intersection b) {
        int dx = a.getX() - b.getX();
        int dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy) / 50.0;
    }

    //stop duplicate roads
    private boolean roadExists(Intersection a, Intersection b) {
        for (Road r : graph.getNeighbors(a)) {
            if (r.end.equals(b)) return true;
        }
        return false;
    }

    //draw
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // ROADS
        for (Intersection i : graph.getIntersections()) {
            for (Road r : graph.getNeighbors(i)) {

                double time = r.distance * r.getCongestionFactor(currentHour);

                if (r.closed) {
                    g.setColor(Color.GRAY);
                } else if (r.congestionFactor > 1.5) {
                    g.setColor(Color.RED);
                } else if (r.tollCost > 0) {
                    g.setColor(Color.ORANGE);
                } else {
                    g.setColor(Color.BLACK);
                }

                g.drawLine(
                        r.start.getX(), r.start.getY(),
                        r.end.getX(), r.end.getY()
                );

                int midX = (r.start.getX() + r.end.getX()) / 2;
                int midY = (r.start.getY() + r.end.getY()) / 2;

                String label = String.format("%.1f", time);

                if (r.tollCost > 0) {
                    label += " ($" + r.tollCost + ")";
                }

                g.drawString(label, midX, midY);
            }
        }

        // SHORTEST PATH
        g.setColor(Color.GREEN);
        for (int i = 0; i < shortestPath.size() - 1; i++) {
            Intersection a = shortestPath.get(i);
            Intersection b = shortestPath.get(i + 1);
            g.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
        }

        // NODES
        for (Intersection i : graph.getIntersections()) {

            if (i.equals(selected)) {
                g.setColor(Color.GREEN);
            } else if (i.equals(startNode)) {
                g.setColor(Color.MAGENTA);
            } else if (i.equals(endNode)) {
                g.setColor(Color.ORANGE);
            } else {
                g.setColor(Color.BLUE);
            }

            g.fillOval(i.getX() - 6, i.getY() - 6, 12, 12);
            g.drawString(i.getId(), i.getX() + 5, i.getY() - 5);
        }
    }

    //clicks
    private Intersection getClickedIntersection(int mouseX, int mouseY) {

        for (Intersection i : graph.getIntersections()) {

            int dx = mouseX - i.getX();
            int dy = mouseY - i.getY();

            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance <= 10) return i;
        }

        return null;
    }

    //label w/ status
    private void updateStatus() {
        statusLabel.setText(
                "Mode: " + mode +
                        " | Hour: " + currentHour +
                        ":00" +
                        " | Avoid Tolls: " + avoidTolls +
                        " | Creating Toll Roads: " + (createTollRoad ? "ON" : "OFF")
        );
    }
}