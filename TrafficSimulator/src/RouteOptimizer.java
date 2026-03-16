import java.util.*;

public class RouteOptimizer {

    public static Map<Intersection, Double> dijkstra(TrafficGraph graph, Intersection start) {

        Map<Intersection, Double> distance = new HashMap<>();
        PriorityQueue<Intersection> pq =
                new PriorityQueue<>(Comparator.comparingDouble(distance::get));

        for (Intersection i : graph.getIntersections()) {
            distance.put(i, Double.MAX_VALUE);
        }

        distance.put(start, 0.0);
        pq.add(start);

        while (!pq.isEmpty()) {

            Intersection current = pq.poll();

            for (Road road : graph.getNeighbors(current)) {

                if (road.closed)
                    continue;

                Intersection neighbor = road.end;

                double newDist =
                        distance.get(current) + road.getTravelTime();

                if (newDist < distance.get(neighbor)) {

                    distance.put(neighbor, newDist);
                    pq.add(neighbor);

                }
            }
        }

        return distance;
    }
}