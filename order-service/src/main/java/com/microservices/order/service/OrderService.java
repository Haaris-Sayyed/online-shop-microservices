package com.microservices.order.service;

import com.microservices.order.client.InventoryClient;
import com.microservices.order.dto.OrderRequest;
import com.microservices.order.event.OrderPlacedEvent;
import com.microservices.order.model.Order;
import com.microservices.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    private final InventoryClient inventoryClient;

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public String placeOrder(OrderRequest orderRequest){
       boolean isProductInStock = inventoryClient.isInStock(orderRequest.skuCode(),orderRequest.quantity());
       if(!isProductInStock){
           throw new RuntimeException("Product with skucode " + orderRequest.skuCode() + " is not in stock");
       }
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setSkuCode(orderRequest.skuCode());
        order.setPrice(orderRequest.price());
        order.setQuantity(orderRequest.quantity());

        // Send the message to Kafka Topic
        OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent();
        orderPlacedEvent.setOrderNumber(order.getOrderNumber());
        orderPlacedEvent.setEmail(orderRequest.userDetails().email());
        orderPlacedEvent.setFirstName(orderRequest.userDetails().firstName());
        orderPlacedEvent.setLastName(orderRequest.userDetails().lastName());
        log.info("Start - Sending OrderPlacedEvent {} to kafka topic order-placed", orderPlacedEvent);
        kafkaTemplate.send("order-placed",orderPlacedEvent);
        log.info("End - Sending OrderPlacedEvent {} to kafka topic order-placed", orderPlacedEvent);

        return "Order placed successfully.";

    }
}
