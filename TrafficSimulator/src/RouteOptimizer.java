import java.util.*;

public class RouteOptimizer {

    public enum RouteMode {
        FASTEST,
        AVOID_TOLLS,
        BALANCED,
        EMERGENCY
    }

    public static List<Intersection> getShortestPath(
            TrafficGraph graph,
            Intersection start,
            Intersection end,
            RouteMode routeMode,
            int hour
    ) {

        Map<Intersection, Double> dist = new HashMap<>();
        Map<Intersection, Intersection> prev = new HashMap<>();

        PriorityQueue<Intersection> pq =
                new PriorityQueue<>(Comparator.comparingDouble(dist::get));

        for (Intersection i : graph.getIntersections()) {
            dist.put(i, Double.POSITIVE_INFINITY);
        }

        dist.put(start, 0.0);
        pq.add(start);

        while (!pq.isEmpty()) {

            Intersection current = pq.poll();

            if (current.equals(end)) break;

            List<Road> edges = new ArrayList<>();

            edges.addAll(graph.getNeighbors(current));

            if (routeMode == RouteMode.EMERGENCY) {
                for (Intersection i : graph.getIntersections()) {
                    for (Road r : graph.getNeighbors(i)) {
                        if (r.end.equals(current)) {
                            Road fake = new Road(r.end, r.start, r.distance);
                            fake.congestionFactor = r.congestionFactor;
                            fake.tollCost = r.tollCost;
                            fake.status = r.status;
                            fake.oneWay = r.oneWay;
                            edges.add(fake);
                        }
                    }
                }
            }

            for (Road r : edges) {

                if (r.status == RoadStatus.CLOSED && routeMode != RouteMode.EMERGENCY) {
                    continue;
                }

                Intersection neighbor = r.end;

                double time = r.distance * r.getCongestionFactor(hour);
                double weight;

                switch (routeMode) {

                    case FASTEST:
                        weight = time;
                        break;

                    case AVOID_TOLLS:
                        if (r.tollCost > 0) continue;
                        weight = time;
                        break;

                    case BALANCED:
                        weight = time + (r.tollCost * 2);
                        break;

                    case EMERGENCY:
                        weight = time * 0.8;
                        break;

                    default:
                        weight = time;
                }

                double newDist = dist.get(current) + weight;

                if (newDist < dist.get(neighbor)) {
                    dist.put(neighbor, newDist);
                    prev.put(neighbor, current);
                    pq.add(neighbor);
                }
            }
        }

        List<Intersection> path = new ArrayList<>();

        if (!prev.containsKey(end) && !start.equals(end)) {
            return path;
        }

        Intersection step = end;

        while (step != null) {
            path.add(0, step);
            step = prev.get(step);
        }

        return path;
    }
}