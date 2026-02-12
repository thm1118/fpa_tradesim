package com.fintech.tradesim.dto;

import com.fintech.tradesim.entity.Order;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequest {
    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotNull(message = "Side is required")
    private Order.OrderSide side;

    @NotNull(message = "Order type is required")
    private Order.OrderType type;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private BigDecimal limitPrice;

    private BigDecimal stopPrice;
}
