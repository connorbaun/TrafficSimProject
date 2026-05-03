import java.util.*;

public class TrafficGraph {

    private Map<Intersection, List<Road>> graph = new HashMap<>();

    public void addIntersection(Intersection i) {
        graph.putIfAbsent(i, new ArrayList<>());
    }

    public void removeIntersection(Intersection i) {

        if (!graph.containsKey(i)) return;

        for (List<Road> roads : graph.values()) {
            roads.removeIf(r -> r.start.equals(i) || r.end.equals(i));
        }

        graph.remove(i);
    }

    public void addRoad(Road r) {

        graph.putIfAbsent(r.start, new ArrayList<>());
        graph.putIfAbsent(r.end, new ArrayList<>());

        graph.get(r.start).add(r);

        if (!r.oneWay) {
            Road reverse = new Road(r.end, r.start, r.distance);
            reverse.congestionFactor = r.congestionFactor;
            reverse.tollCost = r.tollCost;
            reverse.status = r.status;
            reverse.oneWay = false;
            graph.get(r.end).add(reverse);
        }
    }

    public void removeRoad(Intersection a, Intersection b) {
        if (!graph.containsKey(a)) return;

        graph.get(a).removeIf(r -> r.end.equals(b));

        if (graph.containsKey(b)) {
            graph.get(b).removeIf(r -> r.end.equals(a));
        }
    }

    public void removeRoad(Road r) {
        removeRoad(r.start, r.end);
    }

    public List<Road> getNeighbors(Intersection i) {
        return graph.getOrDefault(i, new ArrayList<>());
    }

    public Set<Intersection> getIntersections() {
        return graph.keySet();
    }


}