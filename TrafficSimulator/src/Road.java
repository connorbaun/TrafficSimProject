public class Road {

    Intersection start;
    Intersection end;

    double distance;
    double congestionFactor;
    double tollCost;
    boolean oneWay;

    RoadStatus status;

    public Road(Intersection start, Intersection end, double distance) {
        this.start = start;
        this.end = end;
        this.distance = distance;

        congestionFactor = 1.0;
        tollCost = 0;
        oneWay = false;

        status = RoadStatus.OPEN;
    }

    public double getCongestionFactor(int hour) {

        double factor = congestionFactor;

        if (hour >= 7 && hour <= 9) factor *= 2.0;
        if (hour >= 16 && hour <= 18) factor *= 2.0;

        return factor;
    }

    public double getTravelTime(int hour) {
        return distance * getCongestionFactor(hour);
    }

    public boolean isBlocked() {
        return status != RoadStatus.OPEN;
    }

    public RoadStatus getStatus() {
        return status;
    }

    public void setStatus(RoadStatus status) {
        this.status = status;
    }
}