import java.util.Random;
import java.util.concurrent.BlockingQueue;

class CustomerGenerator extends Thread {
    private final BlockingQueue<Order> orderQueue;
    private final int maxOrders;
    private final Random random = new Random();

    public CustomerGenerator(BlockingQueue<Order> orderQueue, int maxOrders) {
        this.orderQueue = orderQueue;
        this.maxOrders = maxOrders;
        this.setName("CustomerGenerator");
    }

    private Point generateRandomPoint() {
        double x = 10 + random.nextDouble() * 90;
        double y = 10 + random.nextDouble() * 90;
        return new Point(x, y);
    }

    @Override
    public void run() {
        int ordersGenerated = 0;
        System.out.println("[Generator] Генератор клиенто начал работу");
        while (ordersGenerated < maxOrders && !Thread.currentThread().isInterrupted()) {
            try {
                Point pickup = generateRandomPoint();
                Point destination = generateRandomPoint();
                Order order = new Order(pickup, destination);
                orderQueue.put(order);
                System.out.println("[Generator] Создан новый заказ: " + order);
                ordersGenerated++;
                Thread.sleep(200 + random.nextInt(500));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("[Generator] Генератор клиентов закончил свою работу");
    }
}
