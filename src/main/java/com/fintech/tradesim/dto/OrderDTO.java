package com.fintech.tradesim.dto;

import com.fintech.tradesim.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDTO {
    private Long id;
    private String orderNo;
    private String symbol;
    private String securityName;
    private Order.OrderSide side;
    private Order.OrderType type;
    private Integer quantity;
    private Integer filledQuantity;
    private Integer remainingQuantity;
    private BigDecimal limitPrice;
    private BigDecimal stopPrice;
    private BigDecimal avgFillPrice;
    private Order.OrderStatus status;
    private BigDecimal commission;
    private String cancelReason;
    private LocalDateTime createdAt;
    private LocalDateTime filledAt;
}
