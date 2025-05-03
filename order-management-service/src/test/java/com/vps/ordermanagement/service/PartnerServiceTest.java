package com.vps.ordermanagement.service;

import com.vps.ordermanagement.dto.PartnerDTO;
import com.vps.ordermanagement.exception.ResourceNotFoundException;
import com.vps.ordermanagement.model.Partner;
import com.vps.ordermanagement.repository.PartnerRepository;
import com.vps.ordermanagement.service.impl.PartnerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PartnerServiceTest {

    @Mock
    private PartnerRepository partnerRepository;

    @InjectMocks
    private PartnerServiceImpl partnerService;

    private Partner partner;
    private PartnerDTO partnerDTO;

    @BeforeEach
    void setUp() {
        partner = new Partner();
        partner.setId(1L);
        partner.setName("Parceiro Teste");
        partner.setEmail("parceiro@teste.com");
        partner.setDocumentNumber("12345678901");
        partner.setCreditLimit(new BigDecimal("10000.00"));
        partner.setAvailableCredit(new BigDecimal("10000.00"));

        partnerDTO = new PartnerDTO();
        partnerDTO.setId(1L);
        partnerDTO.setName("Parceiro Teste");
        partnerDTO.setEmail("parceiro@teste.com");
        partnerDTO.setDocumentNumber("12345678901");
        partnerDTO.setCreditLimit(new BigDecimal("10000.00"));
        partnerDTO.setAvailableCredit(new BigDecimal("10000.00"));
    }

    @Test
    void getAllPartners_ShouldReturnAllPartners() {
        when(partnerRepository.findAll()).thenReturn(Arrays.asList(partner));

        List<PartnerDTO> partners = partnerService.getAllPartners();

        assertNotNull(partners);
        assertEquals(1, partners.size());
        assertEquals(partner.getId(), partners.get(0).getId());
        assertEquals(partner.getName(), partners.get(0).getName());
    }

    @Test
    void getPartnerById_WithValidId_ShouldReturnPartner() {
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));

        PartnerDTO result = partnerService.getPartnerById(1L);

        assertNotNull(result);
        assertEquals(partner.getId(), result.getId());
        assertEquals(partner.getName(), result.getName());
    }

    @Test
    void getPartnerById_WithInvalidId_ShouldThrowException() {
        when(partnerRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            partnerService.getPartnerById(2L);
        });
    }

    @Test
    void createPartner_ShouldReturnCreatedPartner() {
        when(partnerRepository.save(any(Partner.class))).thenReturn(partner);

        PartnerDTO result = partnerService.createPartner(partnerDTO);

        assertNotNull(result);
        assertEquals(partner.getId(), result.getId());
        assertEquals(partner.getName(), result.getName());
        assertEquals(partner.getCreditLimit(), result.getCreditLimit());
        assertEquals(partner.getAvailableCredit(), result.getAvailableCredit());
    }

    @Test
    void updatePartner_WithValidId_ShouldReturnUpdatedPartner() {
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));
        when(partnerRepository.save(any(Partner.class))).thenReturn(partner);

        PartnerDTO updatedDTO = new PartnerDTO();
        updatedDTO.setId(1L);
        updatedDTO.setName("Parceiro Atualizado");
        updatedDTO.setEmail("atualizado@teste.com");
        updatedDTO.setDocumentNumber("98765432109");
        updatedDTO.setCreditLimit(new BigDecimal("15000.00"));

        PartnerDTO result = partnerService.updatePartner(1L, updatedDTO);

        assertNotNull(result);
        assertEquals(updatedDTO.getName(), result.getName());
        assertEquals(updatedDTO.getEmail(), result.getEmail());
        assertEquals(updatedDTO.getDocumentNumber(), result.getDocumentNumber());
    }

    @Test
    void hasEnoughCredit_WithSufficientCredit_ShouldReturnTrue() {
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));

        boolean result = partnerService.hasEnoughCredit(1L, new BigDecimal("5000.00"));

        assertTrue(result);
    }

    @Test
    void hasEnoughCredit_WithInsufficientCredit_ShouldReturnFalse() {
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));

        boolean result = partnerService.hasEnoughCredit(1L, new BigDecimal("15000.00"));

        assertFalse(result);
    }

    @Test
    void reserveCredit_WithSufficientCredit_ShouldDeductCreditCorrectly() {
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));
        when(partnerRepository.save(any(Partner.class))).thenReturn(partner);

        partnerService.reserveCredit(1L, new BigDecimal("3000.00"));

        assertEquals(new BigDecimal("7000.00"), partner.getAvailableCredit());
    }

    @Test
    void releaseCredit_ShouldAddCreditCorrectly() {
        partner.setAvailableCredit(new BigDecimal("7000.00"));
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));
        when(partnerRepository.save(any(Partner.class))).thenReturn(partner);

        partnerService.releaseCredit(1L, new BigDecimal("2000.00"));

        assertEquals(new BigDecimal("9000.00"), partner.getAvailableCredit());
    }
} 