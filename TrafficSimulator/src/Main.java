public class Main {

    public static void main(String[] args) {

        TrafficGraph graph = new TrafficGraph();

        Intersection A = new Intersection("A");
        Intersection B = new Intersection("B");
        Intersection C = new Intersection("C");
        Intersection D = new Intersection("D");

        graph.addIntersection(A);
        graph.addIntersection(B);
        graph.addIntersection(C);
        graph.addIntersection(D);

        Road r1 = new Road(A, B, 5);
        Road r2 = new Road(A, C, 3);
        Road r3 = new Road(B, D, 4);
        Road r4 = new Road(C, D, 6);

        graph.addRoad(r1);
        graph.addRoad(r2);
        graph.addRoad(r3);
        graph.addRoad(r4);

        r4.congestionFactor = 2.0;

        var result = RouteOptimizer.dijkstra(graph, A);

        System.out.println("Travel time from A to D: " + result.get(D));
    }
}