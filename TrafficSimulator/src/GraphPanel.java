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

    private boolean createTollRoad = false;
    private boolean createOneWay = false;

    private int currentHour = 8;

    private java.util.List<Intersection> shortestPath =
            new java.util.ArrayList<>();

    private RouteOptimizer.RouteMode routeMode =
            RouteOptimizer.RouteMode.FASTEST;

    private enum Mode {
        BUILD,
        PATH
    }

    private Mode mode = Mode.BUILD;

    public GraphPanel(TrafficGraph graph, JLabel statusLabel) {
        this.graph = graph;
        this.statusLabel = statusLabel;

        setBackground(Color.LIGHT_GRAY);

        setFocusable(true);

        //keys to switch modes
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {

                char key = e.getKeyChar();
                int code = e.getKeyCode();

                if (key == 'm') {
                    mode = (mode == Mode.BUILD) ? Mode.PATH : Mode.BUILD;
                }

                else if (key == 'p') {
                    createTollRoad = !createTollRoad;
                }

                else if (key == 'o') {
                    createOneWay = !createOneWay;
                }

                else if (key == '1') routeMode = RouteOptimizer.RouteMode.FASTEST;
                else if (key == '2') routeMode = RouteOptimizer.RouteMode.AVOID_TOLLS;
                else if (key == '3') routeMode = RouteOptimizer.RouteMode.BALANCED;
                else if (key == '4') routeMode = RouteOptimizer.RouteMode.EMERGENCY;

                else if (code == java.awt.event.KeyEvent.VK_UP) {
                    currentHour = (currentHour + 1) % 24;
                }

                else if (code == java.awt.event.KeyEvent.VK_DOWN) {
                    currentHour = (currentHour - 1 + 24) % 24;
                }

                updateStatus();
                repaint();
            }
        });

        //fixed mouse controls
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                int x = e.getX();
                int y = e.getY();

                Intersection clicked = getClickedIntersection(x, y);

                //add a nodes
                if (clicked == null) {

                    if (mode == Mode.PATH) return;

                    String id = "I" + nodeCounter++;
                    graph.addIntersection(new Intersection(id, x, y));

                    selected = null;
                    repaint();
                    return;
                }

                //path mode
                if (mode == Mode.PATH) {

                    if (startNode == null) {
                        startNode = clicked;
                    } else {
                        endNode = clicked;

                        shortestPath = RouteOptimizer.getShortestPath(
                                graph,
                                startNode,
                                endNode,
                                routeMode,
                                currentHour
                        );

                        startNode = null;
                        endNode = null;
                    }
                }

                //build mode
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

                            r.oneWay = createOneWay;

                            graph.addRoad(r);
                        }

                        selected = null;
                    }
                }

                updateStatus();
                repaint();
            }
        });

        requestFocusInWindow();
        updateStatus();
    }

    //distance
    private double calculateDistance(Intersection a, Intersection b) {
        int dx = a.getX() - b.getX();
        int dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy) / 50.0;
    }

    //prevent dupes
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

                if (r.closed) g.setColor(Color.GRAY);
                else if (r.congestionFactor > 1.5) g.setColor(Color.RED);
                else if (r.tollCost > 0) g.setColor(Color.ORANGE);
                else g.setColor(Color.BLACK);

                //drawing the lines (shortened lines to prevent overlaps)
                double dx = r.end.getX() - r.start.getX();
                double dy = r.end.getY() - r.start.getY();

                double len = Math.sqrt(dx*dx + dy*dy);

                double shrink = 12 / len;

                int xEnd = (int)(r.end.getX() - dx * shrink);
                int yEnd = (int)(r.end.getY() - dy * shrink);

                g.drawLine(r.start.getX(), r.start.getY(), xEnd, yEnd);

                if (r.oneWay) {
                    drawArrow(g, r.start.getX(), r.start.getY(), xEnd, yEnd);
                }

                // draw arrow if one-way
                if (r.oneWay) {
                    drawArrow(g,
                            r.start.getX(), r.start.getY(),
                            r.end.getX(), r.end.getY()
                    );
                }

                int midX = (r.start.getX() + r.end.getX()) / 2;
                int midY = (r.start.getY() + r.end.getY()) / 2;

                String label = String.format("%.1f", time);

                if (r.tollCost > 0) {
                    label += " ($" + r.tollCost + ")";
                }

                g.drawString(label, midX, midY);

                if (r.oneWay) {
                    g.setColor(Color.BLUE);
                    g.drawString("→", midX, midY);
                }
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

            if (i.equals(selected)) g.setColor(Color.GREEN);
            else if (i.equals(startNode)) g.setColor(Color.MAGENTA);
            else if (i.equals(endNode)) g.setColor(Color.ORANGE);
            else g.setColor(Color.BLUE);

            g.fillOval(i.getX() - 6, i.getY() - 6, 12, 12);
        }

        updateStatus();
    }

 //clicks
    private Intersection getClickedIntersection(int x, int y) {

        for (Intersection i : graph.getIntersections()) {
            int dx = x - i.getX();
            int dy = y - i.getY();

            if (Math.sqrt(dx * dx + dy * dy) <= 10) {
                return i;
            }
        }
        return null;
    }

//stats bar
private void updateStatus() {
    statusLabel.setText(
            "Mode: " + mode +
                    " | Hour: " + currentHour +
                    " | Route: " + routeMode +
                    " | Toll Roads: " + (createTollRoad ? "ON" : "OFF") +
                    " | One-Way: " + (createOneWay ? "ON" : "OFF")
    );
}

    private void drawArrow(Graphics g, int x1, int y1, int x2, int y2) {

        Graphics2D g2 = (Graphics2D) g.create();

        double angle = Math.atan2(y2 - y1, x2 - x1);

        int arrowLength = 10;
        int arrowAngle = 25; // degrees

        int xA = (int) (x2 - arrowLength * Math.cos(angle - Math.toRadians(arrowAngle)));
        int yA = (int) (y2 - arrowLength * Math.sin(angle - Math.toRadians(arrowAngle)));

        int xB = (int) (x2 - arrowLength * Math.cos(angle + Math.toRadians(arrowAngle)));
        int yB = (int) (y2 - arrowLength * Math.sin(angle + Math.toRadians(arrowAngle)));

        g2.drawLine(x2, y2, xA, yA);
        g2.drawLine(x2, y2, xB, yB);

        g2.dispose();
    }
}