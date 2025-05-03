package com.vps.ordermanagement.service;

import com.vps.ordermanagement.dto.PartnerDTO;

import java.math.BigDecimal;
import java.util.List;

public interface PartnerService {
    
    List<PartnerDTO> getAllPartners();
    
    PartnerDTO getPartnerById(Long id);
    
    PartnerDTO createPartner(PartnerDTO partnerDTO);
    
    PartnerDTO updatePartner(Long id, PartnerDTO partnerDTO);
    
    void deletePartner(Long id);
    
    boolean hasEnoughCredit(Long partnerId, BigDecimal orderValue);
    
    void reserveCredit(Long partnerId, BigDecimal orderValue);
    
    void releaseCredit(Long partnerId, BigDecimal orderValue);
} 