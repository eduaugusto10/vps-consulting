package com.vps.ordermanagement.dto;

import com.vps.ordermanagement.model.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para atualização de status de pedido")
public class OrderStatusUpdateDTO {
    
    @NotNull(message = "Status é obrigatório")
    @Schema(description = "Novo status para o pedido", example = "APROVADO", enumAsRef = true)
    private OrderStatus status;
} 