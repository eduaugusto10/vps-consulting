package com.vps.ordermanagement.service;

import com.vps.ordermanagement.event.OrderEvent;
import org.springframework.kafka.support.Acknowledgment;

public interface KafkaConsumerService {
    void consumeOrderEvent(OrderEvent event, Acknowledgment ack);
    void consumeRetryOrderEvent(OrderEvent event, Acknowledgment ack);
    void consumeDeadLetterOrderEvent(OrderEvent event, Acknowledgment ack);
} 