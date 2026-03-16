import java.util.*;

public class TrafficGraph {

    private Map<Intersection, List<Road>> graph;

    public TrafficGraph() {
        graph = new HashMap<>();
    }

    public void addIntersection(Intersection i) {
        graph.putIfAbsent(i, new ArrayList<>());
    }

    public void addRoad(Road r) {

        graph.get(r.start).add(r);

        if (!r.oneWay) {
            Road reverse = new Road(r.end, r.start, r.distance);
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