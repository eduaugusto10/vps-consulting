package com.vps.ordermanagement.service.impl;

import com.vps.ordermanagement.event.OrderEvent;
import com.vps.ordermanagement.model.enums.OrderStatus;
import com.vps.ordermanagement.service.KafkaConsumerService;
import com.vps.ordermanagement.service.KafkaProducerService;
import com.vps.ordermanagement.service.OrderProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerServiceImpl implements KafkaConsumerService {

    private final OrderProcessingService orderProcessingService;
    private final KafkaProducerService kafkaProducerService;

    @KafkaListener(topics = "${kafka.topics.orders}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeOrderEvent(OrderEvent event, Acknowledgment ack) {
        log.info("Recebido evento: {}, dados: {}", event.getEventType(), event.getOrderData());
        
        try {
            processEvent(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Erro ao processar evento: {}", event, e);
            handleProcessingError(event);
            ack.acknowledge(); // Confirmamos para remover da fila original
        }
    }

    @KafkaListener(topics = "${kafka.topics.orders-retry}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeRetryOrderEvent(OrderEvent event, Acknowledgment ack) {
        log.info("Recebido evento para retry (tentativa {}): {}", event.getRetryCount(), event.getEventType());
        
        try {
            processEvent(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Erro ao processar evento no retry (tentativa {}): {}", event.getRetryCount(), event, e);
            handleProcessingError(event);
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = "${kafka.topics.orders-dead}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeDeadLetterOrderEvent(OrderEvent event, Acknowledgment ack) {
        log.error("Evento na fila DLQ (após {} tentativas): {}", event.getRetryCount(), event);
        
        log.error("EVENTO EM DLQ - REQUER ANÁLISE MANUAL: ID={}, Tipo={}, Tentativas={}, Dados={}",
                event.getEventId(), event.getEventType(), event.getRetryCount(), event.getOrderData());
        
        ack.acknowledge();
    }

    private void processEvent(OrderEvent event) {
        switch (event.getEventType()) {
            case "ORDER_CREATED":
                orderProcessingService.processOrderCreation(event.getOrderData());
                break;
            case "ORDER_UPDATED":
                orderProcessingService.processOrderUpdate(event.getOrderData());
                break;
            case "ORDER_CANCELLED":
                orderProcessingService.processOrderCancellation(event.getOrderData().getId());
                break;
            default:
                log.warn("Tipo de evento desconhecido: {}", event.getEventType());
        }
    }

    private void handleProcessingError(OrderEvent event) {
        int newRetryCount = event.getRetryCount() + 1;
        event.setRetryCount(newRetryCount);
        event.setTimestamp(LocalDateTime.now());
        
        kafkaProducerService.sendOrderEvent(event);
    }
} 