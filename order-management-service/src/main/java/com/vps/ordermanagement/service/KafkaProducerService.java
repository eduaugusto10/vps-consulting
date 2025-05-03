package com.vps.ordermanagement.service;

import com.vps.ordermanagement.event.OrderEvent;

public interface KafkaProducerService {
    void sendOrderEvent(OrderEvent event);
} 