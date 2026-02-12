package com.fintech.tradesim.dto;

import com.fintech.tradesim.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TradeDTO {
    private Long id;
    private String tradeNo;
    private String orderNo;
    private String symbol;
    private String securityName;
    private Order.OrderSide side;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal commission;
    private LocalDateTime executedAt;
}
