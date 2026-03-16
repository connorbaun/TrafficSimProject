import java.util.*;

public class RouteOptimizer {

    // implements djikstra's; returns best distance to each node
    public static Map<Intersection, Double> dijkstra(TrafficGraph graph, Intersection start) {

        // stores distances to each node
        Map<Intersection, Double> distance = new HashMap<>();

        // priority queue removes smallest element first
        // will process the closest intersection each time
        PriorityQueue<Intersection> pq =
                new PriorityQueue<>(Comparator.comparingDouble(distance::get));

        // start each node at infinite distance away
        for (Intersection i : graph.getIntersections()) {
            distance.put(i, Double.MAX_VALUE);
        }

        // starting node has distance of 0.0
        distance.put(start, 0.0);
        pq.add(start);

        // until all reachable nodes are handled...
        while (!pq.isEmpty()) {

            // poll( )  removes the lowest distance node
            Intersection current = pq.poll();

            // checks all roads leaving the current intersection
            for (Road road : graph.getNeighbors(current)) {

                //skip a closed road if we see one
                if (road.closed)
                    continue;

                // if road goes from A-B, then the neighbor is B
                Intersection neighbor = road.end;

                // calculate distance to current node + travel time of that road
                double newDist =
                        distance.get(current) + road.getTravelTime();

                // if a shorter path was found, then update it in 'distance'
                if (newDist < distance.get(neighbor)) {

                    distance.put(neighbor, newDist);
                    pq.add(neighbor);

                }
            }
        }

        return distance;
    }
}