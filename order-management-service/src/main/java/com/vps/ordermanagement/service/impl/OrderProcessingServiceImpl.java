package com.vps.ordermanagement.service.impl;

import com.vps.ordermanagement.dto.OrderResponseDTO;
import com.vps.ordermanagement.dto.OrderStatusUpdateDTO;
import com.vps.ordermanagement.model.enums.OrderStatus;
import com.vps.ordermanagement.service.OrderProcessingService;
import com.vps.ordermanagement.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderProcessingServiceImpl implements OrderProcessingService {

    private final OrderService orderService;
    private final Random random = new Random();

    @Override
    @Transactional
    public void processOrderCreation(OrderResponseDTO orderData) {
        log.info("Processando criação do pedido: {}", orderData.getId());
        
        simulateRandomError(orderData.getId());
        
        log.info("Pedido {} processado com sucesso", orderData.getId());
    }

    @Override
    @Transactional
    public void processOrderUpdate(OrderResponseDTO orderData) {
        log.info("Processando atualização do pedido: {}, novo status: {}", 
                orderData.getId(), orderData.getStatus());
        
        simulateRandomError(orderData.getId());
        
        log.info("Atualização do pedido {} processada com sucesso", orderData.getId());
    }

    @Override
    @Transactional
    public void processOrderCancellation(Long orderId) {
        log.info("Processando cancelamento do pedido: {}", orderId);
        
        simulateRandomError(orderId);
        
        log.info("Cancelamento do pedido {} processado com sucesso", orderId);
    }
    
    private void simulateRandomError(Long orderId) {
        int errorChance = random.nextInt(10);
        if (errorChance < 3) {
            log.error("Erro simulado ao processar o pedido {}", orderId);
            throw new RuntimeException("Erro simulado para demonstrar sistema de retry");
        }
    }
} 