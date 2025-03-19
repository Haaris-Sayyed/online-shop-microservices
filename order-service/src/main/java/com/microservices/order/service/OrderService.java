package com.microservices.order.service;

import com.microservices.order.client.InventoryClient;
import com.microservices.order.dto.OrderRequest;
import com.microservices.order.model.Order;
import com.microservices.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final InventoryClient inventoryClient;

    public String placeOrder(OrderRequest orderRequest){
       boolean isProductInStock = inventoryClient.isInStock(orderRequest.skuCode(),orderRequest.quantity());
       if(!isProductInStock){
           throw new RuntimeException("Product with skucode " + orderRequest.skuCode() + " is not in stock");
       }
        Order order = new Order();
        order.setOrderNumber(orderRequest.orderNumber());
        order.setSkuCode(orderRequest.skuCode());
        order.setPrice(orderRequest.price());
        order.setQuantity(orderRequest.quantity());
        orderRepository.save(order);
        return "Order placed successfully.";

    }
}
