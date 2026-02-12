package com.fintech.tradesim.service;

import com.fintech.tradesim.dto.SecurityDTO;
import com.fintech.tradesim.entity.Security;
import com.fintech.tradesim.exception.ResourceNotFoundException;
import com.fintech.tradesim.repository.SecurityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SecurityService {
    private final SecurityRepository securityRepository;

    public Security getBySymbol(String symbol) {
        return securityRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Security not found: " + symbol));
    }

    public List<SecurityDTO> getAllTradable() {
        return securityRepository.findByTradableTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SecurityDTO> getByType(Security.SecurityType type) {
        return securityRepository.findByType(type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SecurityDTO> search(String keyword) {
        return securityRepository.findByNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SecurityDTO> getTopGainers() {
        return securityRepository.findTopGainers().stream()
                .limit(10)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SecurityDTO> getTopLosers() {
        return securityRepository.findTopLosers().stream()
                .limit(10)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SecurityDTO> getMostActive() {
        return securityRepository.findTopByVolume().stream()
                .limit(10)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public SecurityDTO convertToDTO(Security security) {
        SecurityDTO dto = new SecurityDTO();
        dto.setId(security.getId());
        dto.setSymbol(security.getSymbol());
        dto.setName(security.getName());
        dto.setType(security.getType());
        dto.setExchange(security.getExchange());
        dto.setSector(security.getSector());
        dto.setCurrentPrice(security.getCurrentPrice());
        dto.setPreviousClose(security.getPreviousClose());
        dto.setOpenPrice(security.getOpenPrice());
        dto.setHighPrice(security.getHighPrice());
        dto.setLowPrice(security.getLowPrice());
        dto.setVolume(security.getVolume());
        dto.setChangePercent(security.getChangePercent());

        BigDecimal changeAmount = security.getCurrentPrice().subtract(security.getPreviousClose());
        dto.setChangeAmount(changeAmount);
        dto.setTradable(security.getTradable());

        return dto;
    }
}
