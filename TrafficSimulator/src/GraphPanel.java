    import javax.swing.*;
    import java.awt.*;
    import java.awt.event.MouseAdapter;
    import java.awt.event.MouseEvent;
    import java.util.ArrayList;
    import java.util.List;

    public class GraphPanel extends JPanel {

        private final JLabel statusLabel;
        private final TrafficGraph graph;

        private int nodeCounter = 0;

        private Intersection buildFromNode = null;
        private Intersection startNode = null;
        private Intersection endNode = null;

        private boolean createTollRoad = false;
        private boolean createOneWay = false;

        private int currentHour = 12;

        private List<Intersection> shortestPath = new ArrayList<>();

        private double totalTravelTime = 0.0;
        private double totalTolls = 0.0;

        private RouteOptimizer.RouteMode routeMode = RouteOptimizer.RouteMode.FASTEST;

        private enum Mode { BUILD, PATH }
        private Mode mode = Mode.BUILD;

        private Road selectedRoad = null;

        public GraphPanel(TrafficGraph graph, JLabel statusLabel) {
            this.graph = graph;
            this.statusLabel = statusLabel;

            setBackground(Color.gray);
            setFocusable(true);

            addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent e) {

                    int code = e.getKeyCode();
                    char key = e.getKeyChar();
                    key = Character.toLowerCase(key);

                    if (code == java.awt.event.KeyEvent.VK_ESCAPE) {
                        clearSelection();
                    }

                    if (selectedRoad != null) {

                        if (key == 'c') {
                            selectedRoad.setStatus(RoadStatus.CLOSED);
                        }

                        if (key == 'o') {
                            selectedRoad.setStatus(RoadStatus.OPEN);
                        }

                        if (key == 'n') {
                            selectedRoad.setStatus(RoadStatus.CONSTRUCTION);
                        }

                        if (key == 'a') {
                            selectedRoad.setStatus(RoadStatus.ACCIDENT);
                        }

                        repaint();
                        updateStatus();
                    }

                    if (key == 'm') mode = (mode == Mode.BUILD) ? Mode.PATH : Mode.BUILD;
                    if (key == 'p') createTollRoad = !createTollRoad;
                    if (key == 'o') createOneWay = !createOneWay;

                    if (key == '1') routeMode = RouteOptimizer.RouteMode.FASTEST;
                    if (key == '2') routeMode = RouteOptimizer.RouteMode.AVOID_TOLLS;
                    if (key == '3') routeMode = RouteOptimizer.RouteMode.BALANCED;
                    if (key == '4') routeMode = RouteOptimizer.RouteMode.EMERGENCY;

                    if (code == java.awt.event.KeyEvent.VK_UP) currentHour = (currentHour + 1) % 24;
                    if (code == java.awt.event.KeyEvent.VK_DOWN) currentHour = (currentHour - 1 + 24) % 24;

                    if (key == 'x') {

                        if (selectedRoad != null) {
                            graph.removeRoad(selectedRoad.start, selectedRoad.end);
                            selectedRoad = null;
                        } else if (startNode != null && endNode != null) {
                            graph.removeRoad(startNode, endNode);
                        } else if (buildFromNode != null) {
                            graph.removeIntersection(buildFromNode);
                            buildFromNode = null;
                        }

                        shortestPath.clear();
                        clearInvalidState();
                        updateStatus();
                        repaint();
                    }

                    updateStatus();
                    repaint();
                }
            });

            addMouseListener(new MouseAdapter() {

                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                }

                public void mouseClicked(MouseEvent e) {

                    int x = e.getX();
                    int y = e.getY();

                    if (mode == Mode.BUILD) {
                        Road clickedRoad = getClickedRoad(x, y);
                        if (clickedRoad != null) {
                            selectedRoad = clickedRoad;
                            updateStatus();
                            repaint();
                            return;
                        }
                    }

                    Intersection clicked = getClickedIntersection(x, y);

                    if (mode == Mode.PATH) {

                        if (clicked == null) return;

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

                            totalTravelTime = computeTotalTime(shortestPath);
                            totalTolls = computeTotalTolls(shortestPath);

                            startNode = null;
                            endNode = null;
                        }

                        updateStatus();
                        repaint();
                        return;
                    }

                    if (clicked == null) {

                        String id = "I" + nodeCounter++;
                        graph.addIntersection(new Intersection(id, x, y));

                        buildFromNode = null;
                        repaint();
                        return;
                    }

                    if (buildFromNode == null) {
                        buildFromNode = clicked;
                    } else {

                        if (!buildFromNode.equals(clicked)) {

                            if (!roadExists(buildFromNode, clicked)) {

                                double dist = calculateDistance(buildFromNode, clicked);

                                Road r = new Road(buildFromNode, clicked, dist);

                                if (createTollRoad) r.tollCost = 3.0;
                                r.oneWay = createOneWay;

                                graph.addRoad(r);
                            }
                        }

                        buildFromNode = null;
                    }

                    updateStatus();
                    repaint();
                }
            });

            SwingUtilities.invokeLater(this::requestFocusInWindow);
            updateStatus();
        }

        private void clearInvalidState() {

            if (buildFromNode != null && !graph.getIntersections().contains(buildFromNode)) buildFromNode = null;
            if (startNode != null && !graph.getIntersections().contains(startNode)) startNode = null;
            if (endNode != null && !graph.getIntersections().contains(endNode)) endNode = null;

            if (selectedRoad != null) {
                boolean valid = false;
                for (Road r : graph.getNeighbors(selectedRoad.start)) {
                    if (r.end.equals(selectedRoad.end)) valid = true;
                }
                if (!valid) selectedRoad = null;
            }
        }

        private double computeTotalTime(List<Intersection> path) {
            double sum = 0.0;

            for (int i = 0; i < path.size() - 1; i++) {
                Intersection a = path.get(i);
                Intersection b = path.get(i + 1);

                for (Road r : graph.getNeighbors(a)) {
                    if (r.end.equals(b)) {
                        sum += r.distance * r.getCongestionFactor(currentHour);
                        break;
                    }
                }
            }

            return sum;
        }

        private double computeTotalTolls(List<Intersection> path) {

            if (routeMode == RouteOptimizer.RouteMode.EMERGENCY) {
                return 0.0;
            }

            double sum = 0.0;

            for (int i = 0; i < path.size() - 1; i++) {
                Intersection a = path.get(i);
                Intersection b = path.get(i + 1);

                for (Road r : graph.getNeighbors(a)) {
                    if (r.end.equals(b)) {
                        sum += r.tollCost;
                        break;
                    }
                }
            }

            return sum;
        }

        private double calculateDistance(Intersection a, Intersection b) {
            int dx = a.getX() - b.getX();
            int dy = a.getY() - b.getY();
            return Math.sqrt(dx * dx + dy * dy) / 50.0;
        }

        private boolean roadExists(Intersection a, Intersection b) {
            for (Road r : graph.getNeighbors(a)) {
                if (r.end.equals(b)) return true;
            }
            return false;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (Intersection i : graph.getIntersections()) {
                for (Road r : graph.getNeighbors(i)) {

                    int x1 = r.start.getX();
                    int y1 = r.start.getY();
                    int x2 = r.end.getX();
                    int y2 = r.end.getY();

                    double time = r.distance * r.getCongestionFactor(currentHour);

                    if (r.getStatus() == RoadStatus.CLOSED && routeMode != RouteOptimizer.RouteMode.EMERGENCY) {
                        g2.setColor(Color.DARK_GRAY);
                    } else if (r.getStatus() == RoadStatus.CONSTRUCTION) {
                        g2.setColor(Color.GRAY);
                    } else if (r.getStatus() == RoadStatus.ACCIDENT) {
                        g2.setColor(Color.PINK);
                    } else if (r.getCongestionFactor(currentHour) > 1.5) {
                        g2.setColor(Color.RED);
                    } else if (r.tollCost > 0) {
                        g2.setColor(Color.ORANGE);
                    } else {
                        g2.setColor(Color.BLACK);
                    }

                    g2.setStroke(r.equals(selectedRoad) ? new BasicStroke(4) : new BasicStroke(1));
                    g2.drawLine(x1, y1, x2, y2);
                    // draw arrow if one-way
                    if (r.oneWay) {
                        drawArrowHead(g2, x1, y1+4, x2, y2+4);
                    }

                    drawRoadStatus(g2, r, x1, y1, x2, y2);

                    int mx = (x1 + x2) / 2;
                    int my = (y1 + y2) / 2;

                    String label = String.format("%.1f", time);

                    if (routeMode != RouteOptimizer.RouteMode.EMERGENCY && r.tollCost > 0) {
                        label += " $" + r.tollCost;
                    }
                    g2.setFont(new Font("Arial", Font.BOLD, 16));

                    g2.drawString(label, mx, my);
                }
            }

            g2.setColor(Color.GREEN);
            for (int i = 0; i < shortestPath.size() - 1; i++) {
                Intersection a = shortestPath.get(i);
                Intersection b = shortestPath.get(i + 1);

                g2.setStroke(new BasicStroke(3));
                g2.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
                g2.setStroke(new BasicStroke(1));
            }

            for (Intersection i : graph.getIntersections()) {

                if (i.equals(buildFromNode)) g2.setColor(Color.GREEN);
                else if (i.equals(startNode)) g2.setColor(Color.MAGENTA);
                else if (i.equals(endNode)) g2.setColor(Color.ORANGE);
                else g2.setColor(Color.BLUE);

                g2.fillOval(i.getX() - 6, i.getY() - 6, 12, 12);
            }
        }

        private Intersection getClickedIntersection(int x, int y) {
            for (Intersection i : graph.getIntersections()) {
                int dx = x - i.getX();
                int dy = y - i.getY();
                if (Math.sqrt(dx * dx + dy * dy) <= 10) return i;
            }
            return null;
        }

        private Road getClickedRoad(int x, int y) {

            double threshold = 6.0;
            double nodeBlockRadius = 14.0;

            for (Intersection i : graph.getIntersections()) {
                if (Math.hypot(x - i.getX(), y - i.getY()) <= nodeBlockRadius) return null;
            }

            for (Intersection i : graph.getIntersections()) {
                for (Road r : graph.getNeighbors(i)) {

                    int x1 = r.start.getX();
                    int y1 = r.start.getY();
                    int x2 = r.end.getX();
                    int y2 = r.end.getY();

                    double dx = x2 - x1;
                    double dy = y2 - y1;

                    double t = ((x - x1) * dx + (y - y1) * dy) / (dx * dx + dy * dy);
                    t = Math.max(0, Math.min(1, t));

                    double px = x1 + t * dx;
                    double py = y1 + t * dy;

                    if (Math.hypot(x - px, y - py) <= threshold) return r;
                }
            }

            return null;
        }

        private void updateStatus() {
            statusLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            statusLabel.setText(
                    "Mode: " + mode +
                            " | Hour: " + currentHour +
                            " | Route: " + routeMode +
                            " | Toll: " + (createTollRoad ? "ON" : "OFF") +
                            " | OneWay: " + (createOneWay ? "ON" : "OFF") +
                            " | SelectedRoad: " + (selectedRoad != null ? selectedRoad.getStatus() : "NONE") +
                            " | Total travel time: " + String.format("%.2f", totalTravelTime) +
                            " | Total tolls: " + (routeMode == RouteOptimizer.RouteMode.EMERGENCY ? "NO TOLLS FOR EMS" : String.format("%.2f", totalTolls))
            );
        }

        private void clearSelection() {
            selectedRoad = null;
            startNode = null;
            endNode = null;
            buildFromNode = null;
            shortestPath.clear();
            totalTravelTime = 0.0;
            totalTolls = 0.0;
        }

        private void drawArrowHead(Graphics2D g2, int x1, int y1, int x2, int y2) {

            double phi = Math.toRadians(25);
            int barb = 12;

            double dy = y2 - y1;
            double dx = x2 - x1;
            double theta = Math.atan2(dy, dx);

            double x, y;

            // left side of arrow
            x = x2 - barb * Math.cos(theta + phi);
            y = y2 - barb * Math.sin(theta + phi);
            g2.drawLine(x2, y2, (int) x, (int) y);

            // right side of arrow
            x = x2 - barb * Math.cos(theta - phi);
            y = y2 - barb * Math.sin(theta - phi);
            g2.drawLine(x2, y2, (int) x, (int) y);

        }

        private void drawRoadStatus(Graphics2D g2, Road r, int x1, int y1, int x2, int y2) {

            RoadStatus status = r.getStatus();

            // don't clutter normal roads unless you want to
            if (status == RoadStatus.OPEN) return;

            int mx = (x1 + x2) / 2;
            int my = (y1 + y2) / 2;

            String text = status.toString();

            // color per status
            switch (status) {
                case CLOSED -> g2.setColor(Color.DARK_GRAY);
                case CONSTRUCTION -> g2.setColor(Color.ORANGE);
                case ACCIDENT -> g2.setColor(Color.PINK);
                default -> g2.setColor(Color.BLACK);
            }
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString(text, mx + 5, my - 5);
        }
    }