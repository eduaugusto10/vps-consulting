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
import com.vps.ordermanagement.model.enums.OrderStatus;
import com.vps.ordermanagement.repository.OrderRepository;
import com.vps.ordermanagement.repository.PartnerRepository;
import com.vps.ordermanagement.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PartnerRepository partnerRepository;

    @Mock
    private PartnerService partnerService;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Partner partner;
    private Order order;
    private OrderItem orderItem;
    private OrderRequestDTO orderRequestDTO;
    private OrderItemDTO orderItemDTO;

    @BeforeEach
    void setUp() {
        partner = new Partner();
        partner.setId(1L);
        partner.setName("Parceiro Teste");
        partner.setCreditLimit(new BigDecimal("10000.00"));
        partner.setAvailableCredit(new BigDecimal("10000.00"));

        orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setProductCode("PROD-001");
        orderItem.setProductName("Produto Teste");
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(new BigDecimal("100.00"));

        order = new Order();
        order.setId(1L);
        order.setPartner(partner);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalValue(new BigDecimal("200.00"));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.getItems().add(orderItem);
        orderItem.setOrder(order);

        orderItemDTO = new OrderItemDTO();
        orderItemDTO.setProductCode("PROD-001");
        orderItemDTO.setProductName("Produto Teste");
        orderItemDTO.setQuantity(2);
        orderItemDTO.setUnitPrice(new BigDecimal("100.00"));

        orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setPartnerId(1L);
        orderRequestDTO.setItems(Arrays.asList(orderItemDTO));
    }

    @Test
    void getOrderById_WithValidId_ShouldReturnOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponseDTO result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(order.getId(), result.getId());
        assertEquals(order.getPartner().getId(), result.getPartnerId());
        assertEquals(order.getStatus(), result.getStatus());
        assertEquals(order.getTotalValue(), result.getTotalValue());
    }

    @Test
    void getOrderById_WithInvalidId_ShouldThrowException() {
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderById(2L);
        });
    }

    @Test
    void createOrder_WithValidData_ShouldCreateOrder() {
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));
        when(partnerService.hasEnoughCredit(1L, new BigDecimal("200.00"))).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });

        OrderResponseDTO result = orderService.createOrder(orderRequestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getPartnerId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(new BigDecimal("200.00"), result.getTotalValue());
        assertEquals(1, result.getItems().size());

        verify(notificationService, times(1)).notifyOrderStatusChange(any(Order.class));
    }

    @Test
    void createOrder_WithInsufficientCredit_ShouldThrowException() {
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));
        when(partnerService.hasEnoughCredit(1L, new BigDecimal("200.00"))).thenReturn(false);

        assertThrows(InsufficientCreditException.class, () -> {
            orderService.createOrder(orderRequestDTO);
        });

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_FromPendingToApproved_ShouldUpdateAndReserveCredit() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderStatusUpdateDTO statusUpdateDTO = new OrderStatusUpdateDTO(OrderStatus.APPROVED);

        OrderResponseDTO result = orderService.updateOrderStatus(1L, statusUpdateDTO);

        assertEquals(OrderStatus.APPROVED, result.getStatus());
        verify(partnerService, times(1)).reserveCredit(1L, new BigDecimal("200.00"));
        verify(notificationService, times(1)).notifyOrderStatusChange(order);
    }

    @Test
    void updateOrderStatus_FromApprovedToCancelled_ShouldUpdateAndReleaseCredit() {
        order.setStatus(OrderStatus.APPROVED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderStatusUpdateDTO statusUpdateDTO = new OrderStatusUpdateDTO(OrderStatus.CANCELLED);

        OrderResponseDTO result = orderService.updateOrderStatus(1L, statusUpdateDTO);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(partnerService, times(1)).releaseCredit(1L, new BigDecimal("200.00"));
        verify(notificationService, times(1)).notifyOrderStatusChange(order);
    }

    @Test
    void updateOrderStatus_FromCancelledToApproved_ShouldThrowException() {
        order.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderStatusUpdateDTO statusUpdateDTO = new OrderStatusUpdateDTO(OrderStatus.APPROVED);

        assertThrows(InvalidOrderStatusException.class, () -> {
            orderService.updateOrderStatus(1L, statusUpdateDTO);
        });

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_WithPendingOrder_ShouldCancelOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(partnerService, never()).releaseCredit(anyLong(), any(BigDecimal.class));
        verify(notificationService, times(1)).notifyOrderStatusChange(order);
    }

    @Test
    void cancelOrder_WithApprovedOrder_ShouldCancelOrderAndReleaseCredit() {
        order.setStatus(OrderStatus.APPROVED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(partnerService, times(1)).releaseCredit(1L, new BigDecimal("200.00"));
        verify(notificationService, times(1)).notifyOrderStatusChange(order);
    }

    @Test
    void cancelOrder_WithShippedOrder_ShouldThrowException() {
        order.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusException.class, () -> {
            orderService.cancelOrder(1L);
        });

        verify(orderRepository, never()).save(any(Order.class));
    }
} 