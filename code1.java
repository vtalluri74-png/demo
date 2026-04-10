import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

// --- Model ---
class Order {
    private final int id;
    private final double amount;

    public Order(int id, double amount) {
        this.id = id;
        this.amount = amount;
    }

    public int getId() { return id; }
    public double getAmount() { return amount; }

    @Override
    public String toString() {
        return "Order{id=" + id + ", amount=" + amount + "}";
    }
}

// --- Interface (abstraction) ---
interface PaymentProcessor {
    void process(Order order) throws Exception;
}

// --- Implementation ---
class UpiPaymentProcessor implements PaymentProcessor {
    @Override
    public void process(Order order) throws Exception {
        if (order.getAmount() <= 0) {
            throw new Exception("Invalid amount for order " + order.getId());
        }
        System.out.println("Processing UPI payment for " + order);
    }
}

// --- Service Layer ---
class OrderService {
    private final PaymentProcessor processor;

    public OrderService(PaymentProcessor processor) {
        this.processor = processor;
    }

    public void processOrders(List<Order> orders) {
        // Filter + Stream API
        List<Order> validOrders = orders.stream()
                .filter(o -> o.getAmount() > 0)
                .collect(Collectors.toList());

        // Multithreading using ExecutorService
        ExecutorService executor = Executors.newFixedThreadPool(3);

        for (Order order : validOrders) {
            executor.submit(() -> {
                try {
                    processor.process(order);
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
    }
}

// --- Main Class ---
public class Main {
    public static void main(String[] args) {
        List<Order> orders = Arrays.asList(
                new Order(1, 500),
                new Order(2, -100), // invalid
                new Order(3, 1200),
                new Order(4, 0)     // invalid
        );

        PaymentProcessor processor = new UpiPaymentProcessor();
        OrderService service = new OrderService(processor);

        service.processOrders(orders);
    }
}
