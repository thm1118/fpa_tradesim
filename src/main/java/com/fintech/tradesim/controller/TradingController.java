package com.fintech.tradesim.controller;

import com.fintech.tradesim.client.RiskControlClient;
import com.fintech.tradesim.dto.OrderDTO;
import com.fintech.tradesim.dto.OrderRequest;
import com.fintech.tradesim.dto.TradeDTO;
import com.fintech.tradesim.security.CurrentUser;
import com.fintech.tradesim.security.UserPrincipal;
import com.fintech.tradesim.service.TradingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trading")
@RequiredArgsConstructor
public class TradingController {
    private final TradingService tradingService;
    private final RiskControlClient riskControlClient;

    @PostMapping("/orders")
    public ResponseEntity<OrderDTO> placeOrder(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody OrderRequest request) {
        OrderDTO order = tradingService.placeOrder(principal.getUser(), request);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/orders/{orderNo}")
    public ResponseEntity<OrderDTO> cancelOrder(
            @CurrentUser UserPrincipal principal,
            @PathVariable String orderNo) {
        OrderDTO order = tradingService.cancelOrder(principal.getUser(), orderNo);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/orders")
    public ResponseEntity<Page<OrderDTO>> getOrders(
            @CurrentUser UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(tradingService.getOrders(principal.getUser(), pageable));
    }

    @GetMapping("/orders/active")
    public ResponseEntity<List<OrderDTO>> getActiveOrders(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(tradingService.getActiveOrders(principal.getUser()));
    }

    @GetMapping("/trades")
    public ResponseEntity<Page<TradeDTO>> getTrades(
            @CurrentUser UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(tradingService.getTrades(principal.getUser(), pageable));
    }

    @PostMapping("/risk-check")
    public ResponseEntity<Map<String, Object>> riskCheck(
            @CurrentUser UserPrincipal principal,
            @RequestBody Map<String, Object> request) {
        String username = principal.getUser().getUsername();
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        Map<String, Object> result = riskControlClient.checkTransaction(username, "TRADE", amount, null);
        return ResponseEntity.ok(result);
    }
}
