package com.vps.ordermanagement.dto;

import com.vps.ordermanagement.model.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para resposta de pedido criado ou consultado")
public class OrderResponseDTO {
    
    @Schema(description = "ID do pedido", example = "1")
    private Long id;
    
    @Schema(description = "ID do parceiro", example = "1")
    private Long partnerId;
    
    @Schema(description = "Nome do parceiro", example = "Empresa ABC Ltda")
    private String partnerName;
    
    @Schema(description = "Lista de itens do pedido")
    private List<OrderItemDTO> items;
    
    @Schema(description = "Valor total do pedido", example = "1999.99")
    private BigDecimal totalValue;
    
    @Schema(description = "Status do pedido", example = "PENDING", enumAsRef = true)
    private OrderStatus status;
    
    @Schema(description = "Data e hora de criação do pedido", example = "2023-05-01T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Data e hora da última atualização do pedido", example = "2023-05-01T14:45:00")
    private LocalDateTime updatedAt;
} 