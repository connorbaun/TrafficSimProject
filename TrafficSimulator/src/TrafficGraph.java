import java.util.*;

public class TrafficGraph {

    // intersections are nodes, values = list of roads that connect to the intersection
    private Map<Intersection, List<Road>> graph;

    // create an empty graph
    public TrafficGraph() {
        graph = new HashMap<>();
    }

    // add a node if it is not already in the graph w/ empty list of roads
    public void addIntersection(Intersection i) {
        graph.putIfAbsent(i, new ArrayList<>());
    }


    public void addRoad(Road r) {

        // find list of roads that start from the 'start' node intersection
        // add given road 'r' to that list of roads
        graph.get(r.start).add(r);

        //if road is a two-way:
            // add the "reverse way" for bi-directional roads as well as its own road
        if (!r.oneWay) {
            Road reverse = new Road(r.end, r.start, r.distance);
            reverse.congestionFactor = r.congestionFactor;
            reverse.tollCost = r.tollCost;
            reverse.closed = r.closed;
            reverse.oneWay = r.oneWay;
            graph.get(r.end).add(reverse);
        }
    }

    // gets list of all roads possible to take from Intersection 'i'
    public List<Road> getNeighbors(Intersection i) {
        return graph.get(i);
    }

    // for the graph, get ALL intersections in the graph in unordered set
    public Set<Intersection> getIntersections() {
        return graph.keySet();
    }
}