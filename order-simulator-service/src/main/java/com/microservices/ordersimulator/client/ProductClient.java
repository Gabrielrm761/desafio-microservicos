package com.microservices.ordersimulator.client;

import com.microservices.ordersimulator.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/products")
    List<Product> getAllProducts();

    @GetMapping("/api/products/{id}")
    Product getProductById(@PathVariable("id") Long id);

    @GetMapping("/api/products/available")
    List<Product> getAvailableProducts();

    @GetMapping("/api/products/category/{category}")
    List<Product> getProductsByCategory(@PathVariable("category") String category);

    @PutMapping("/api/products/{id}/stock")
    Map<String, Object> updateStock(@PathVariable("id") Long id, @RequestBody Map<String, Integer> request);
}