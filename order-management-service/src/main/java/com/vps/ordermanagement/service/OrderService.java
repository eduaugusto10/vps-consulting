package com.vps.ordermanagement.service;

import com.vps.ordermanagement.dto.OrderItemDTO;
import com.vps.ordermanagement.dto.OrderRequestDTO;
import com.vps.ordermanagement.dto.OrderResponseDTO;
import com.vps.ordermanagement.dto.OrderStatusUpdateDTO;
import com.vps.ordermanagement.exception.InsufficientCreditException;
import com.vps.ordermanagement.exception.InvalidOrderStatusException;
import com.vps.ordermanagement.exception.ResourceNotFoundException;
import com.vps.ordermanagement.model.Order;
import com.vps.ordermanagement.model.OrderItem;
import com.vps.ordermanagement.model.Partner;
import com.vps.ordermanagement.repository.OrderRepository;
import com.vps.ordermanagement.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vps.ordermanagement.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public interface OrderService {
    
    List<OrderResponseDTO> getAllOrders();
    
    OrderResponseDTO getOrderById(Long id);
    
    List<OrderResponseDTO> getOrdersByPartnerId(Long partnerId);
    
    List<OrderResponseDTO> getOrdersByStatus(OrderStatus status);
    
    List<OrderResponseDTO> getOrdersByDateRange(LocalDateTime start, LocalDateTime end);
    
    OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO);
    
    OrderResponseDTO updateOrderStatus(Long id, OrderStatusUpdateDTO statusUpdateDTO);
    
    void cancelOrder(Long id);
} 