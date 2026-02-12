package com.fintech.tradesim.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionDTO {
    private Long id;
    private String symbol;
    private String securityName;
    private Integer quantity;
    private Integer availableQuantity;
    private Integer frozenQuantity;
    private BigDecimal avgCost;
    private BigDecimal currentPrice;
    private BigDecimal totalCost;
    private BigDecimal marketValue;
    private BigDecimal unrealizedProfit;
    private BigDecimal profitRate;
}
