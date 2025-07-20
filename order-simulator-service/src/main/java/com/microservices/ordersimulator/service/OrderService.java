package com.microservices.ordersimulator.service;

import com.microservices.ordersimulator.client.ProductClient;
import com.microservices.ordersimulator.model.Order;
import com.microservices.ordersimulator.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {

    @Autowired
    private ProductClient productClient;

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public Order createOrder(Order order) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<String> errors = new ArrayList<>();

        for (Order.OrderItem item : order.getItems()) {
            try {
                Product product = productClient.getProductById(item.getProductId());
                if (product == null) {
                    errors.add("Product not found: " + item.getProductId());
                    continue;
                }

                if (product.getStock() < item.getQuantity()) {
                    errors.add("Insufficient stock for product: " + product.getName());
                    continue;
                }

                item.setProductName(product.getName());
                item.setUnitPrice(product.getPrice());
                item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                totalAmount = totalAmount.add(item.getSubtotal());

                Map<String, Integer> stockUpdate = Map.of("quantity", item.getQuantity());
                Map<String, Object> result = productClient.updateStock(item.getProductId(), stockUpdate);
                
                if (!(Boolean) result.get("success")) {
                    errors.add("Failed to update stock for product: " + product.getName());
                }

            } catch (Exception e) {
                errors.add("Error processing product " + item.getProductId() + ": " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException("Order creation failed: " + String.join(", ", errors));
        }

        order.setTotalAmount(totalAmount);
        order.setStatus("CONFIRMED");
        orders.put(order.getOrderId(), order);

        return order;
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    public Optional<Order> getOrderById(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    public Order simulateRandomOrder() {
        try {
            List<Product> availableProducts = productClient.getAvailableProducts();
            if (availableProducts.isEmpty()) {
                throw new RuntimeException("No available products to create order");
            }

            Random random = new Random();
            Order order = new Order();
            order.setCustomerName("Customer-" + random.nextInt(1000));

            List<Order.OrderItem> items = new ArrayList<>();
            int numberOfItems = random.nextInt(3) + 1;

            for (int i = 0; i < numberOfItems; i++) {
                Product randomProduct = availableProducts.get(random.nextInt(availableProducts.size()));
                int quantity = random.nextInt(Math.min(randomProduct.getStock(), 5)) + 1;

                Order.OrderItem item = new Order.OrderItem();
                item.setProductId(randomProduct.getId());
                item.setQuantity(quantity);
                items.add(item);
            }

            order.setItems(items);
            return createOrder(order);

        } catch (Exception e) {
            throw new RuntimeException("Failed to simulate random order: " + e.getMessage());
        }
    }

    public List<Product> getAvailableProducts() {
        return productClient.getAvailableProducts();
    }

    public List<Product> getProductsByCategory(String category) {
        return productClient.getProductsByCategory(category);
    }
}