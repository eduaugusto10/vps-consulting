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
@Schema(description = "DTO para parceiro comercial")
public class PartnerDTO {
    
    @Schema(description = "ID do parceiro (gerado automaticamente)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @NotBlank(message = "Nome é obrigatório")
    @Schema(description = "Nome do parceiro", example = "Empresa ABC Ltda")
    private String name;
    
    @NotBlank(message = "Email é obrigatório")
    @Schema(description = "Email do parceiro", example = "contato@empresaabc.com.br")
    private String email;
    
    @NotBlank(message = "Número do documento é obrigatório")
    @Schema(description = "CNPJ ou CPF do parceiro", example = "12.345.678/0001-00")
    private String documentNumber;
    
    @NotNull(message = "Limite de crédito é obrigatório")
    @Positive(message = "Limite de crédito deve ser maior que zero")
    @Schema(description = "Limite total de crédito do parceiro", example = "50000.00")
    private BigDecimal creditLimit;
    
    @Schema(description = "Crédito disponível atual (calculado automaticamente)", example = "35000.00", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal availableCredit;
} 