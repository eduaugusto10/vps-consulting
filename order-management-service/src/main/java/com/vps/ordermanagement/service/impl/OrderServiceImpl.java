package com.vps.ordermanagement.service.impl;

import com.vps.ordermanagement.dto.OrderItemDTO;
import com.vps.ordermanagement.dto.OrderRequestDTO;
import com.vps.ordermanagement.dto.OrderResponseDTO;
import com.vps.ordermanagement.dto.OrderStatusUpdateDTO;
import com.vps.ordermanagement.event.OrderEvent;
import com.vps.ordermanagement.model.enums.OrderStatus;
import com.vps.ordermanagement.exception.InsufficientCreditException;
import com.vps.ordermanagement.exception.InvalidOrderStatusException;
import com.vps.ordermanagement.exception.ResourceNotFoundException;
import com.vps.ordermanagement.model.Order;
import com.vps.ordermanagement.model.OrderItem;
import com.vps.ordermanagement.model.Partner;
import com.vps.ordermanagement.repository.OrderRepository;
import com.vps.ordermanagement.repository.PartnerRepository;
import com.vps.ordermanagement.service.KafkaProducerService;
import com.vps.ordermanagement.service.NotificationService;
import com.vps.ordermanagement.service.OrderService;
import com.vps.ordermanagement.service.PartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PartnerRepository partnerRepository;
    private final PartnerService partnerService;
    private final NotificationService notificationService;
    private final KafkaProducerService kafkaProducerService;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado com ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByPartnerId(Long partnerId) {
        return orderRepository.findByPartnerId(partnerId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByCreatedAtBetween(start, end)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {
        Partner partner = findPartnerById(orderRequestDTO.getPartnerId());
        BigDecimal totalValue = calculateTotalValue(orderRequestDTO);
        validatePartnerCredit(partner.getId(), totalValue);
        
        Order order = createOrderWithItems(partner, totalValue, orderRequestDTO);
        Order savedOrder = orderRepository.save(order);
        notificationService.notifyOrderStatusChange(savedOrder);
        
        OrderResponseDTO responseDTO = toResponseDTO(savedOrder);
        sendOrderCreatedEvent(responseDTO);

        return responseDTO;
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long id, OrderStatusUpdateDTO statusUpdateDTO) {
        Order order = findOrderById(id);
        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = statusUpdateDTO.getStatus();

        validateStatusTransition(oldStatus, newStatus);
        handleCreditOnStatusChange(order, oldStatus, newStatus);
        
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        notificationService.notifyOrderStatusChange(updatedOrder);
        
        OrderResponseDTO responseDTO = toResponseDTO(updatedOrder);
        sendOrderUpdatedEvent(responseDTO);

        return responseDTO;
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        Order order = findOrderById(id);
        validateOrderCancellation(order);
        
        if (order.getStatus() == OrderStatus.APPROVED) {
            partnerService.releaseCredit(order.getPartner().getId(), order.getTotalValue());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);
        notificationService.notifyOrderStatusChange(cancelledOrder);
        
        sendOrderCancelledEvent(toResponseDTO(cancelledOrder));
    }

    private Partner findPartnerById(Long partnerId) {
        return partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Parceiro não encontrado com ID: " + partnerId));
    }
    
    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado com ID: " + orderId));
    }
    
    private BigDecimal calculateTotalValue(OrderRequestDTO orderRequestDTO) {
        return orderRequestDTO.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private void validatePartnerCredit(Long partnerId, BigDecimal totalValue) {
        if (!partnerService.hasEnoughCredit(partnerId, totalValue)) {
            throw new InsufficientCreditException("Crédito insuficiente para o parceiro ID: " + partnerId);
        }
    }
    
    private Order createOrderWithItems(Partner partner, BigDecimal totalValue, OrderRequestDTO orderRequestDTO) {
        Order order = new Order();
        order.setPartner(partner);
        order.setTotalValue(totalValue);
        order.setStatus(OrderStatus.PENDING);

        orderRequestDTO.getItems().forEach(itemDTO -> {
            OrderItem item = new OrderItem();
            item.setProductCode(itemDTO.getProductCode());
            item.setProductName(itemDTO.getProductName());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice());
            order.addItem(item);
        });
        
        return order;
    }
    
    private void handleCreditOnStatusChange(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        if (newStatus == OrderStatus.APPROVED && oldStatus != OrderStatus.APPROVED) {
            partnerService.reserveCredit(order.getPartner().getId(), order.getTotalValue());
        }

        if (newStatus == OrderStatus.CANCELLED && oldStatus == OrderStatus.APPROVED) {
            partnerService.releaseCredit(order.getPartner().getId(), order.getTotalValue());
        }
    }
    
    private void validateOrderCancellation(Order order) {
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStatusException("Não é possível cancelar um pedido com status " + order.getStatus());
        }
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == OrderStatus.CANCELLED) {
            throw new InvalidOrderStatusException("Não é possível alterar o status de um pedido cancelado");
        }

        if (currentStatus == OrderStatus.DELIVERED) {
            throw new InvalidOrderStatusException("Não é possível alterar o status de um pedido já entregue");
        }

        if (currentStatus == OrderStatus.SHIPPED && 
            (newStatus == OrderStatus.PENDING || newStatus == OrderStatus.APPROVED || newStatus == OrderStatus.PROCESSING)) {
            throw new InvalidOrderStatusException("Não é possível retroceder o status de um pedido enviado");
        }

        if (currentStatus == OrderStatus.PROCESSING && 
            (newStatus == OrderStatus.PENDING)) {
            throw new InvalidOrderStatusException("Não é possível retroceder para o status PENDING de um pedido em processamento");
        }
    }
    
    private void sendOrderCreatedEvent(OrderResponseDTO responseDTO) {
        OrderEvent event = OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(OrderEvent.EventType.ORDER_CREATED.name())
                .orderData(responseDTO)
                .retryCount(0)
                .timestamp(LocalDateTime.now())
                .build();
        
        kafkaProducerService.sendOrderEvent(event);
    }

    private void sendOrderUpdatedEvent(OrderResponseDTO responseDTO) {
        OrderEvent event = OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(OrderEvent.EventType.ORDER_UPDATED.name())
                .orderData(responseDTO)
                .retryCount(0)
                .timestamp(LocalDateTime.now())
                .build();
        
        kafkaProducerService.sendOrderEvent(event);
    }
    
    private void sendOrderCancelledEvent(OrderResponseDTO responseDTO) {
        OrderEvent event = OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(OrderEvent.EventType.ORDER_CANCELLED.name())
                .orderData(responseDTO)
                .retryCount(0)
                .timestamp(LocalDateTime.now())
                .build();
        
        kafkaProducerService.sendOrderEvent(event);
    }

    private OrderResponseDTO toResponseDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> new OrderItemDTO(
                        item.getId(),
                        item.getProductCode(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice()))
                .collect(Collectors.toList());

        return new OrderResponseDTO(
                order.getId(),
                order.getPartner().getId(),
                order.getPartner().getName(),
                itemDTOs,
                order.getTotalValue(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
} 