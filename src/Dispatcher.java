import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class Dispatcher {
    private final BlockingQueue<Order> orderQueue;
    private final List<Taxi> taxis;
    private final Map<Taxi, Order> taxiAssignments = new ConcurrentHashMap<>();
    private final ReentrantLock assignmentLock = new ReentrantLock();

    public Dispatcher(BlockingQueue<Order> orderQueue, List<Taxi> taxis) {
        this.orderQueue = orderQueue;
        this.taxis = taxis;
    }

    public Order waitForOrder(Taxi taxi) throws InterruptedException {
        while (taxi.isAvailable()) {
            Order order = orderQueue.poll(100, TimeUnit.MILLISECONDS);
            if (order != null && assignOrderToTaxi(order)) {
                return order;
            }
        }
        return null;
    }

    private boolean assignOrderToTaxi(Order order) {
        assignmentLock.lock();
        try {
            Taxi closestTaxi = null;
            double minDistance = Double.MAX_VALUE;

            for (Taxi taxi : taxis) {
                if (taxi.isAvailable()) {
                    double distance = taxi.getCurrentLocation().distanceTo(order.getPickupLocation());
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestTaxi = taxi;
                    }
                }
            }

            if (closestTaxi != null && closestTaxi.tryAssignOrder(order)) {
                taxiAssignments.put(closestTaxi, order);
                System.out.printf("[Dispatcher] Assigned %s to %s (distance: %.1f km)%n",
                        order, closestTaxi.getName(), minDistance);
                return true;
            }

            orderQueue.offer(order);
            return false;

        } finally {
            assignmentLock.unlock();
        }
    }

    public void notifyOrderCompleted(Taxi taxi, Order order) {
        assignmentLock.lock();
        try {
            taxiAssignments.remove(taxi);
            System.out.printf("[Dispatcher] Order#%d completed by %s%n",
                    order.getId(), taxi.getName());
        } finally {
            assignmentLock.unlock();
        }
    }

    public void printStatus() {
        System.out.println("\n=== Dispatcher Status ===");
        System.out.println("Orders in queue: " + orderQueue.size());
        System.out.println("Active assignments: " + taxiAssignments.size());

        for (Taxi taxi : taxis) {
            System.out.println(taxi);
        }
        System.out.println("========================\n");
    }
}