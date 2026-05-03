import java.util.*;

public class TrafficGraph {

    private Map<Intersection, List<Road>> graph = new HashMap<>();

    public void addIntersection(Intersection i) {
        graph.putIfAbsent(i, new ArrayList<>());
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

    public List<Road> getNeighbors(Intersection i) {
        return graph.get(i);
    }

    public Set<Intersection> getIntersections() {
        return graph.keySet();
    }
}