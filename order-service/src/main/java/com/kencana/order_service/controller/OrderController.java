package com.kencana.order_service.controller;

import com.kencana.order_service.client.InventoryClient;
import com.kencana.order_service.model.Order;
import com.kencana.order_service.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final InventoryClient inventoryClient;
    private final OrderRepository orderRepository;

    // Constructor Manual untuk Dependency Injection (Pengganti Lombok)
    public OrderController(InventoryClient inventoryClient, OrderRepository orderRepository) {
        this.inventoryClient = inventoryClient;
        this.orderRepository = orderRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String placeOrder(@RequestBody Order order) {
        // Validasi stok melalui Feign Client ke Inventory Service
        boolean inStock = inventoryClient.checkStock(order.getSkuCode());
        
        if (inStock) {
            orderRepository.save(order);
            return "Order Berhasil Dibuat & Stok Tersedia!";
        } else {
            return "Order Gagal: Stok Barang Habis atau Tidak Ditemukan!";
        }
    }
}