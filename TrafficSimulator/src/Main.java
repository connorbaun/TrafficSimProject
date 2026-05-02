import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {

        TrafficGraph graph = new TrafficGraph();

        // intersections given x and y pos
        Intersection t1 = new Intersection("test1", 100, 100);
        Intersection t2 = new Intersection("test2", 300, 100);

        graph.addIntersection(t1);
        graph.addIntersection(t2);

        Road r1 = new Road(t1, t2, 5);

        // testing a congested road
        r1.congestionFactor = 2.0;
        graph.addRoad(r1);

        // GUI
        JLabel statusLabel = new JLabel();

        GraphPanel panel = new GraphPanel(graph, statusLabel);

        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());

        frame.add(statusLabel, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);

        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}