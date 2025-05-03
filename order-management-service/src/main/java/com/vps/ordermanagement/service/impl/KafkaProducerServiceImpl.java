package com.vps.ordermanagement.service.impl;

import com.vps.ordermanagement.event.OrderEvent;
import com.vps.ordermanagement.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Value("${kafka.topics.orders}")
    private String ordersTopic;

    @Value("${kafka.topics.orders-retry}")
    private String ordersRetryTopic;

    @Value("${kafka.topics.orders-dead}")
    private String ordersDeadTopic;

    @Override
    public void sendOrderEvent(OrderEvent event) {
        String key = UUID.randomUUID().toString();
        
        String topicToUse = getTopic(event);
        
        CompletableFuture<SendResult<String, OrderEvent>> future = kafkaTemplate.send(topicToUse, key, event);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Enviado evento para {} com key: {}, evento: {}", 
                        topicToUse, key, event.getEventType());
            } else {
                log.error("Erro ao enviar evento para {} com key: {}, evento: {}", 
                        topicToUse, key, event.getEventType(), ex);
            }
        });
    }
    
    private String getTopic(OrderEvent event) {
        if (event.getRetryCount() == 0) {
            return ordersTopic;
        } else if (event.getRetryCount() <= 2) {
            return ordersRetryTopic;
        } else {
            return ordersDeadTopic;
        }
    }
} 