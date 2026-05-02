import java.util.*;

public class RouteOptimizer {

    public static List<Intersection> getShortestPath(
            TrafficGraph graph,
            Intersection start,
            Intersection end,
            boolean avoidTolls,
            int hour
    ) {

        // distance table
        Map<Intersection, Double> dist = new HashMap<>();

        // backtracking path
        Map<Intersection, Intersection> prev = new HashMap<>();

        PriorityQueue<Intersection> pq =
                new PriorityQueue<>(Comparator.comparingDouble(dist::get));

        // initialize distances
        for (Intersection i : graph.getIntersections()) {
            dist.put(i, Double.POSITIVE_INFINITY);
        }

        dist.put(start, 0.0);
        pq.add(start);

        while (!pq.isEmpty()) {

            Intersection current = pq.poll();

            // reached destination early?
            if (current.equals(end)) break;

            for (Road r : graph.getNeighbors(current)) {

                if (r.closed) continue;

                if (avoidTolls && r.tollCost > 0) {
                    continue;
                }

                Intersection neighbor = r.end;

                double newDist = dist.get(current)
                        + r.distance * r.getCongestionFactor(hour);

                if (newDist < dist.get(neighbor)) {
                    dist.put(neighbor, newDist);
                    prev.put(neighbor, current);
                    pq.add(neighbor);
                }
            }
        }

        // reconstruct path
        List<Intersection> path = new ArrayList<>();

        Intersection step = end;

        if (!prev.containsKey(end) && !start.equals(end)) {
            return path; // no path found
        }

        while (step != null) {
            path.add(0, step);
            step = prev.get(step);
        }

        return path;
    }
}