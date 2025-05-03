package com.vps.ordermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para item de pedido")
public class OrderItemDTO {
    
    @Schema(description = "ID do item (gerado automaticamente)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @NotBlank(message = "Código do produto é obrigatório")
    @Schema(description = "Código único do produto", example = "PROD-001")
    private String productCode;
    
    @NotBlank(message = "Nome do produto é obrigatório")
    @Schema(description = "Nome do produto", example = "Smartphone XYZ")
    private String productName;
    
    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser maior que zero")
    @Schema(description = "Quantidade do produto", example = "2")
    private Integer quantity;
    
    @NotNull(message = "Preço unitário é obrigatório")
    @Positive(message = "Preço unitário deve ser maior que zero")
    @Schema(description = "Preço unitário do produto", example = "999.99")
    private BigDecimal unitPrice;
    
    public OrderItemDTO(String productCode, String productName, Integer quantity, BigDecimal unitPrice) {
        this.productCode = productCode;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    @Schema(description = "Subtotal do item (calculado automaticamente)", accessMode = Schema.AccessMode.READ_ONLY)
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
} 