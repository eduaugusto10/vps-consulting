package com.vps.ordermanagement.service.impl;

import com.vps.ordermanagement.dto.PartnerDTO;
import com.vps.ordermanagement.exception.ResourceNotFoundException;
import com.vps.ordermanagement.model.Partner;
import com.vps.ordermanagement.repository.PartnerRepository;
import com.vps.ordermanagement.service.PartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PartnerDTO> getAllPartners() {
        return partnerRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerDTO getPartnerById(Long id) {
        return partnerRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Parceiro não encontrado com ID: " + id));
    }

    @Override
    @Transactional
    public PartnerDTO createPartner(PartnerDTO partnerDTO) {
        Partner partner = toEntity(partnerDTO);
        partner.setAvailableCredit(partner.getCreditLimit());
        return toDTO(partnerRepository.save(partner));
    }

    @Override
    @Transactional
    public PartnerDTO updatePartner(Long id, PartnerDTO partnerDTO) {
        return partnerRepository.findById(id)
                .map(partner -> {
                    partner.setName(partnerDTO.getName());
                    partner.setEmail(partnerDTO.getEmail());
                    partner.setDocumentNumber(partnerDTO.getDocumentNumber());
                    
                    BigDecimal oldLimit = partner.getCreditLimit();
                    BigDecimal newLimit = partnerDTO.getCreditLimit();
                    BigDecimal used = oldLimit.subtract(partner.getAvailableCredit());
                    
                    partner.setCreditLimit(newLimit);
                    partner.setAvailableCredit(newLimit.subtract(used));
                    
                    return toDTO(partnerRepository.save(partner));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Parceiro não encontrado com ID: " + id));
    }

    @Override
    @Transactional
    public void deletePartner(Long id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parceiro não encontrado com ID: " + id));
        partnerRepository.delete(partner);
    }

    @Override
    @Transactional
    public boolean hasEnoughCredit(Long partnerId, BigDecimal orderValue) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Parceiro não encontrado com ID: " + partnerId));
        
        return partner.getAvailableCredit().compareTo(orderValue) >= 0;
    }

    @Override
    @Transactional
    public void reserveCredit(Long partnerId, BigDecimal orderValue) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Parceiro não encontrado com ID: " + partnerId));
        
        BigDecimal newAvailableCredit = partner.getAvailableCredit().subtract(orderValue);
        
        if (newAvailableCredit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Crédito insuficiente para o parceiro ID: " + partnerId);
        }
        
        partner.setAvailableCredit(newAvailableCredit);
        partnerRepository.save(partner);
    }

    @Override
    @Transactional
    public void releaseCredit(Long partnerId, BigDecimal orderValue) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Parceiro não encontrado com ID: " + partnerId));
        
        BigDecimal newAvailableCredit = partner.getAvailableCredit().add(orderValue);
        if (newAvailableCredit.compareTo(partner.getCreditLimit()) > 0) {
            newAvailableCredit = partner.getCreditLimit();
        }
        
        partner.setAvailableCredit(newAvailableCredit);
        partnerRepository.save(partner);
    }

    private PartnerDTO toDTO(Partner partner) {
        return new PartnerDTO(
                partner.getId(),
                partner.getName(),
                partner.getEmail(),
                partner.getDocumentNumber(),
                partner.getCreditLimit(),
                partner.getAvailableCredit()
        );
    }

    private Partner toEntity(PartnerDTO dto) {
        Partner partner = new Partner();
        partner.setId(dto.getId());
        partner.setName(dto.getName());
        partner.setEmail(dto.getEmail());
        partner.setDocumentNumber(dto.getDocumentNumber());
        partner.setCreditLimit(dto.getCreditLimit());
        partner.setAvailableCredit(dto.getAvailableCredit() != null ? dto.getAvailableCredit() : dto.getCreditLimit());
        return partner;
    }
} 