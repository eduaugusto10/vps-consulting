package com.vps.ordermanagement.repository;

import com.vps.ordermanagement.model.Order;
import com.vps.ordermanagement.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByPartnerId(Long partnerId);
    
    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<Order> findByPartnerIdAndStatus(Long partnerId, OrderStatus status);
    
    List<Order> findByPartnerIdAndCreatedAtBetween(Long partnerId, LocalDateTime start, LocalDateTime end);
} 