import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


class Taxi extends Thread {
    private final int id;
    private volatile boolean isAvailable = true;
    private volatile Point currentLocation;
    private Order currentOrder;
    private final Dispatcher dispatcher;
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicInteger completedOrders = new AtomicInteger(0);

    public Taxi(int id, Point startLocation, Dispatcher dispatcher) {
        this.id = id;
        this.currentLocation = startLocation;
        this.dispatcher = dispatcher;
        this.setName("Taxi-" + id);
    }

    public int getTaxiId() { return id; }
    public Point getCurrentLocation() { return currentLocation; }
    public boolean isAvailable() { return isAvailable; }
    public int getCompletedOrders() { return completedOrders.get(); }

    public void setAvailable(boolean available) {
        lock.lock();
        try {
            this.isAvailable = available;
        } finally {
            lock.unlock();
        }
    }

    public boolean tryAssignOrder(Order order) {
        if (lock.tryLock()) {
            try {
                if (isAvailable) {
                    this.currentOrder = order;
                    this.isAvailable = false;
                    return true;
                }
                return false;
            } finally {
                lock.unlock();
            }
        }
        return false;
    }

    private void driveTo(Point destination, String action) throws InterruptedException {
        double distance = currentLocation.distanceTo(destination);
        System.out.printf("[%s] %s to %s (distance: %.1f km)%n",
                getName(), action, destination, distance);

        Thread.sleep((long)(distance * 100));
        currentLocation = destination;
    }

    @Override
    public void run() {
        System.out.printf("[%s] Started at location %s%n", getName(), currentLocation);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Order order = dispatcher.waitForOrder(this);
                if (order == null) continue;

                System.out.printf("[%s] Received %s%n", getName(), order);

                driveTo(order.getPickupLocation(), "Driving to pickup");
                System.out.printf("[%s] Arrived at pickup point%n", getName());

                driveTo(order.getDestination(), "Transporting passenger");
                System.out.printf("[%s] Arrived at destination%n", getName());

                completedOrders.incrementAndGet();
                setAvailable(true);
                dispatcher.notifyOrderCompleted(this, order);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.printf("[%s] Was interrupted%n", getName());
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Taxi#%d [Location: %s, Available: %s, Completed: %d]",
                id, currentLocation, isAvailable, completedOrders.get());
    }
}