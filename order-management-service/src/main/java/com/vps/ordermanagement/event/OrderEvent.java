package com.vps.ordermanagement.event;

import com.vps.ordermanagement.dto.OrderResponseDTO;
import com.vps.ordermanagement.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String eventId;
    private String eventType;
    private OrderResponseDTO orderData;
    private int retryCount;
    private LocalDateTime timestamp;
    
    public enum EventType {
        ORDER_CREATED,
        ORDER_UPDATED,
        ORDER_CANCELLED
    }
} 