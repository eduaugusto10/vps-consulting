package com.vps.ordermanagement.controller;

import com.vps.ordermanagement.dto.OrderRequestDTO;
import com.vps.ordermanagement.dto.OrderResponseDTO;
import com.vps.ordermanagement.dto.OrderStatusUpdateDTO;
import com.vps.ordermanagement.exception.InsufficientCreditException;
import com.vps.ordermanagement.exception.InvalidOrderStatusException;
import com.vps.ordermanagement.exception.ResourceNotFoundException;
import com.vps.ordermanagement.model.enums.OrderStatus;
import com.vps.ordermanagement.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "API para gerenciamento de pedidos")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Listar todos os pedidos", description = "Recupera uma lista de todos os pedidos do sistema")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos recuperada com sucesso")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID", description = "Recupera um pedido específico pelo seu ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado", 
                    content = @Content(schema = @Schema(implementation = ResourceNotFoundException.class)))
    })
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @Parameter(description = "ID do pedido", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/partner/{partnerId}")
    @Operation(summary = "Buscar pedidos por ID do parceiro", description = "Recupera todos os pedidos de um parceiro específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de pedidos do parceiro recuperada com sucesso")
    })
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByPartnerId(
            @Parameter(description = "ID do parceiro", required = true)
            @PathVariable Long partnerId) {
        return ResponseEntity.ok(orderService.getOrdersByPartnerId(partnerId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Buscar pedidos por status", description = "Recupera todos os pedidos que estão em um determinado status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de pedidos por status recuperada com sucesso")
    })
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(
            @Parameter(description = "Status do pedido", required = true)
            @PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Buscar pedidos por intervalo de data", 
              description = "Recupera todos os pedidos criados dentro de um intervalo de datas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de pedidos por data recuperada com sucesso")
    })
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByDateRange(
            @Parameter(description = "Data inicial (formato ISO)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "Data final (formato ISO)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(orderService.getOrdersByDateRange(start, end));
    }

    @PostMapping
    @Operation(summary = "Criar novo pedido", 
              description = "Cria um novo pedido verificando o crédito disponível do parceiro")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Crédito insuficiente ou dados inválidos", 
                    content = @Content(schema = @Schema(implementation = InsufficientCreditException.class))),
        @ApiResponse(responseCode = "404", description = "Parceiro não encontrado", 
                    content = @Content(schema = @Schema(implementation = ResourceNotFoundException.class)))
    })
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Parameter(description = "Dados do pedido", required = true)
            @Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        return new ResponseEntity<>(orderService.createOrder(orderRequestDTO), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/status/{status}")
    @Operation(summary = "Atualizar status do pedido", 
              description = "Atualiza o status de um pedido, realizando as operações de crédito necessárias")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status do pedido atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Transição de status inválida", 
                    content = @Content(schema = @Schema(implementation = InvalidOrderStatusException.class))),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado", 
                    content = @Content(schema = @Schema(implementation = ResourceNotFoundException.class)))
    })
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @Parameter(description = "ID do pedido", required = true)
            @PathVariable Long id,
            @Parameter(description = "Novo status do pedido", required = true, schema = @Schema(implementation = OrderStatus.class))
            @PathVariable OrderStatus status) {
        OrderStatusUpdateDTO statusUpdateDTO = new OrderStatusUpdateDTO(status);
        return ResponseEntity.ok(orderService.updateOrderStatus(id, statusUpdateDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar pedido", 
              description = "Cancela um pedido, estornando o crédito se necessário")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Pedido cancelado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Não é possível cancelar o pedido no status atual", 
                    content = @Content(schema = @Schema(implementation = InvalidOrderStatusException.class))),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado", 
                    content = @Content(schema = @Schema(implementation = ResourceNotFoundException.class)))
    })
    public ResponseEntity<Void> cancelOrder(
            @Parameter(description = "ID do pedido", required = true)
            @PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
} 