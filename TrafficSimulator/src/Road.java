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

    public double getCongestionFactor(int hour) {

        // default
        double factor = congestionFactor;

        // rush hour model
        if (hour >= 7 && hour <= 9) {
            factor *= 2.0;
        }

        if (hour >= 16 && hour <= 18) {
            factor *= 2.0;
        }

        return factor;
    }

    // return the time for travel based on congestion and distance
    public double getTravelTime(int hour) {
        return distance * getCongestionFactor(hour);
    }
}