package com.vps.ordermanagement.controller;

import com.vps.ordermanagement.dto.PartnerDTO;
import com.vps.ordermanagement.exception.ResourceNotFoundException;
import com.vps.ordermanagement.service.PartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partners")
@RequiredArgsConstructor
@Tag(name = "Parceiros", description = "API para gerenciamento de parceiros e seus limites de crédito")
public class PartnerController {

    private final PartnerService partnerService;

    @GetMapping
    @Operation(summary = "Listar todos os parceiros", 
              description = "Recupera a lista de todos os parceiros cadastrados no sistema")
    @ApiResponse(responseCode = "200", description = "Lista de parceiros recuperada com sucesso")
    public ResponseEntity<List<PartnerDTO>> getAllPartners() {
        return ResponseEntity.ok(partnerService.getAllPartners());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar parceiro por ID", 
              description = "Recupera um parceiro específico pelo seu ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Parceiro encontrado"),
        @ApiResponse(responseCode = "404", description = "Parceiro não encontrado", 
                    content = @Content(schema = @Schema(implementation = ResourceNotFoundException.class)))
    })
    public ResponseEntity<PartnerDTO> getPartnerById(
            @Parameter(description = "ID do parceiro", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(partnerService.getPartnerById(id));
    }

    @PostMapping
    @Operation(summary = "Criar novo parceiro", 
              description = "Cadastra um novo parceiro no sistema com seu limite de crédito")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Parceiro criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<PartnerDTO> createPartner(
            @Parameter(description = "Dados do parceiro", required = true)
            @Valid @RequestBody PartnerDTO partnerDTO) {
        return new ResponseEntity<>(partnerService.createPartner(partnerDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar parceiro existente", 
              description = "Atualiza os dados de um parceiro existente, incluindo seu limite de crédito")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Parceiro atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Parceiro não encontrado", 
                    content = @Content(schema = @Schema(implementation = ResourceNotFoundException.class)))
    })
    public ResponseEntity<PartnerDTO> updatePartner(
            @Parameter(description = "ID do parceiro", required = true)
            @PathVariable Long id,
            @Parameter(description = "Dados atualizados do parceiro", required = true)
            @Valid @RequestBody PartnerDTO partnerDTO) {
        return ResponseEntity.ok(partnerService.updatePartner(id, partnerDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir parceiro", 
              description = "Remove um parceiro do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Parceiro excluído com sucesso"),
        @ApiResponse(responseCode = "404", description = "Parceiro não encontrado", 
                    content = @Content(schema = @Schema(implementation = ResourceNotFoundException.class)))
    })
    public ResponseEntity<Void> deletePartner(
            @Parameter(description = "ID do parceiro", required = true)
            @PathVariable Long id) {
        partnerService.deletePartner(id);
        return ResponseEntity.noContent().build();
    }
} 