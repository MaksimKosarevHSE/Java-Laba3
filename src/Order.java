import java.util.concurrent.atomic.AtomicInteger;

class Order {
    private final int id;
    private final Point pickupLocation;
    private final Point destination;
    private final long timestamp;
    private static final AtomicInteger idGenerator = new AtomicInteger(1);

    public Order(Point pickupLocation, Point destination) {
        this.id = idGenerator.getAndIncrement();
        this.pickupLocation = pickupLocation;
        this.destination = destination;
        this.timestamp = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public Point getPickupLocation() {
        return pickupLocation;
    }

    public Point getDestination() {
        return destination;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getDistance() {
        return pickupLocation.distanceTo(destination);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", pickupLocation=" + pickupLocation +
                ", destination=" + destination +
                ", timestamp=" + timestamp +
                '}';
    }
}