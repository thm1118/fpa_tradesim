package com.fintech.tradesim.controller;

import com.fintech.tradesim.dto.PositionDTO;
import com.fintech.tradesim.entity.Account;
import com.fintech.tradesim.entity.User;
import com.fintech.tradesim.repository.PositionRepository;
import com.fintech.tradesim.repository.UserRepository;
import com.fintech.tradesim.service.AccountService;
import com.fintech.tradesim.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 内部服务间调用接口 —— 供SmartWallet查询用户投资组合市值
 */
@RestController
@RequestMapping("/internal/account")
@RequiredArgsConstructor
public class InternalAccountController {

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final PositionRepository positionRepository;
    private final PositionService positionService;

    @Value("${service.internal-key}")
    private String internalKey;

    /**
     * 获取用户投资组合总市值 —— 供SmartWallet资产总览聚合
     */
    @GetMapping("/portfolio-value")
    public ResponseEntity<?> getPortfolioValue(
            @RequestHeader("X-Service-Key") String serviceKey,
            @RequestParam String username) {

        validateServiceKey(serviceKey);

        return userRepository.findByUsername(username)
                .map(user -> {
                    Account account = accountService.getAccountByUser(user);
                    BigDecimal stockValue = positionRepository.sumMarketValueByAccount(account);
                    if (stockValue == null) stockValue = BigDecimal.ZERO;

                    return ResponseEntity.ok(Map.of(
                            "available", true,
                            "username", username,
                            "totalAssets", account.getTotalAssets(),
                            "cashBalance", account.getCashBalance(),
                            "stockValue", stockValue,
                            "totalProfit", account.getTotalProfit(),
                            "profitRate", account.getProfitRate()
                    ));
                })
                .orElse(ResponseEntity.ok(Map.of(
                        "available", false,
                        "message", "User not found in TradeSim"
                )));
    }

    /**
     * 获取用户持仓列表 —— 供SmartWallet资产总览聚合
     */
    @GetMapping("/positions")
    public ResponseEntity<?> getPositions(
            @RequestHeader("X-Service-Key") String serviceKey,
            @RequestParam String username) {

        validateServiceKey(serviceKey);

        return userRepository.findByUsername(username)
                .map(user -> {
                    Account account = accountService.getAccountByUser(user);
                    List<PositionDTO> positions = positionService.getPositions(account);
                    return ResponseEntity.ok(positions);
                })
                .orElse(ResponseEntity.ok(List.of()));
    }

    private void validateServiceKey(String serviceKey) {
        if (!internalKey.equals(serviceKey)) {
            throw new IllegalArgumentException("Invalid service key");
        }
    }
}
