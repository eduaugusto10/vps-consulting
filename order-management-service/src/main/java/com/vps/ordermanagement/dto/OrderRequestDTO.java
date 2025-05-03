package com.vps.ordermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para criação de um novo pedido")
public class OrderRequestDTO {
    
    @NotNull(message = "ID do parceiro é obrigatório")
    @Schema(description = "ID do parceiro que está fazendo o pedido", example = "1")
    private Long partnerId;
    
    @NotEmpty(message = "Lista de itens não pode ser vazia")
    @Valid
    @Schema(description = "Lista de itens do pedido")
    private List<OrderItemDTO> items;
} 