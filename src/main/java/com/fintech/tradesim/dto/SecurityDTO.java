package com.fintech.tradesim.dto;

import com.fintech.tradesim.entity.Security;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SecurityDTO {
    private Long id;
    private String symbol;
    private String name;
    private Security.SecurityType type;
    private String exchange;
    private String sector;
    private BigDecimal currentPrice;
    private BigDecimal previousClose;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private Long volume;
    private BigDecimal changePercent;
    private BigDecimal changeAmount;
    private Boolean tradable;
}
