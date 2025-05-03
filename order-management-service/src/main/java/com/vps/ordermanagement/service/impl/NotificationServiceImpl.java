package com.vps.ordermanagement.service.impl;

import com.vps.ordermanagement.model.Order;
import com.vps.ordermanagement.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void notifyOrderStatusChange(Order order) {
        log.info("Enviando notificação - Pedido #{}: Status alterado para {}", 
                order.getId(), order.getStatus());
        
        log.info("Mensagem enviada para o tópico 'order-status-changes': " +
                "{ orderId: " + order.getId() + 
                ", partnerId: " + order.getPartner().getId() + 
                ", status: " + order.getStatus() + 
                ", updatedAt: " + order.getUpdatedAt() + " }");
    }
} 