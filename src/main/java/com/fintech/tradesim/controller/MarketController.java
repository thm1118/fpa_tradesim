package com.fintech.tradesim.controller;

import com.fintech.tradesim.dto.SecurityDTO;
import com.fintech.tradesim.entity.Security;
import com.fintech.tradesim.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketController {
    private final SecurityService securityService;

    @GetMapping("/securities")
    public ResponseEntity<List<SecurityDTO>> getAllSecurities() {
        return ResponseEntity.ok(securityService.getAllTradable());
    }

    @GetMapping("/securities/{symbol}")
    public ResponseEntity<SecurityDTO> getSecurity(@PathVariable String symbol) {
        Security security = securityService.getBySymbol(symbol);
        return ResponseEntity.ok(securityService.convertToDTO(security));
    }

    @GetMapping("/securities/type/{type}")
    public ResponseEntity<List<SecurityDTO>> getSecuritiesByType(@PathVariable String type) {
        Security.SecurityType securityType = Security.SecurityType.valueOf(type.toUpperCase());
        return ResponseEntity.ok(securityService.getByType(securityType));
    }

    @GetMapping("/search")
    public ResponseEntity<List<SecurityDTO>> searchSecurities(@RequestParam String keyword) {
        return ResponseEntity.ok(securityService.search(keyword));
    }

    @GetMapping("/top-gainers")
    public ResponseEntity<List<SecurityDTO>> getTopGainers() {
        return ResponseEntity.ok(securityService.getTopGainers());
    }

    @GetMapping("/top-losers")
    public ResponseEntity<List<SecurityDTO>> getTopLosers() {
        return ResponseEntity.ok(securityService.getTopLosers());
    }

    @GetMapping("/most-active")
    public ResponseEntity<List<SecurityDTO>> getMostActive() {
        return ResponseEntity.ok(securityService.getMostActive());
    }
}
