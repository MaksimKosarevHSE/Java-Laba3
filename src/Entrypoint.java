import java.util.*;
import java.util.concurrent.*;

public class Entrypoint {
    public static void main(String[] args) {
        try {
            BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>(20);

            List<Taxi> taxis = new ArrayList<>();
            Dispatcher dispatcher = new Dispatcher(orderQueue, taxis);

            for (int i = 1; i <= 5; i++) {
                Point startLocation = new Point(i * 15.0, i * 10.0);
                Taxi taxi = new Taxi(i, startLocation, dispatcher);
                taxis.add(taxi);
            }

            CustomerGenerator generator = new CustomerGenerator(orderQueue, 15);
            // Вот тут будет создано 15 заказов. После 15 заказов в консоли будут винды просто логи статуса!

            generator.start();
            for (Taxi taxi : taxis) {
                taxi.start();
            }

            Timer statusTimer = new Timer();
            statusTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    dispatcher.printStatus();
                }
            }, 0, 5000);

            generator.join();
            while (!orderQueue.isEmpty() ||
                    taxis.stream().anyMatch(t -> !t.isAvailable())) {
                Thread.sleep(1000);
            }

            statusTimer.cancel();
            for (Taxi taxi : taxis) {
                taxi.interrupt();
            }

            System.out.println("\n=== Final Statistics ===");
            for (Taxi taxi : taxis) {
                System.out.printf("%s completed %d orders%n",
                        taxi.getName(), taxi.getCompletedOrders());
            }
            System.out.println("System shutdown completed.");
        } catch (InterruptedException ex) {
            System.out.println("Произошла системная ошибка " + ex);
        }
    }
}