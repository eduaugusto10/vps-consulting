package com.vps.ordermanagement.service;

import com.vps.ordermanagement.dto.OrderResponseDTO;

public interface OrderProcessingService {
    void processOrderCreation(OrderResponseDTO orderData);
    void processOrderUpdate(OrderResponseDTO orderData);
    void processOrderCancellation(Long orderId);
} 