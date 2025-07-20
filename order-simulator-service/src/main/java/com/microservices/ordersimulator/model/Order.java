package com.microservices.ordersimulator.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private String orderId;
    
    @NotBlank(message = "Customer name is required")
    private String customerName;
    
    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItem> items;
    
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private String status;

    public Order() {
        this.orderId = generateOrderId();
        this.orderDate = LocalDateTime.now();
        this.status = "PENDING";
    }

    private String generateOrderId() {
        return "ORD-" + System.currentTimeMillis();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static class OrderItem {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        private String productName;
        private BigDecimal unitPrice;
        
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
        
        private BigDecimal subtotal;

        public OrderItem() {}

        public OrderItem(Long productId, String productName, BigDecimal unitPrice, Integer quantity) {
            this.productId = productId;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }
    }
}