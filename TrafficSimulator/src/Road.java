public class Road {

    Intersection start;
    Intersection end;

    double distance;
    double congestionFactor;

    double tollCost;

    boolean closed;
    boolean oneWay;

    public Road(Intersection start, Intersection end, double distance) {
        this.start = start;
        this.end = end;
        this.distance = distance;

        congestionFactor = 1.0;
        tollCost = 0;
        closed = false;
        oneWay = false;
    }

    public double getTravelTime() {
        return distance * congestionFactor;
    }
}