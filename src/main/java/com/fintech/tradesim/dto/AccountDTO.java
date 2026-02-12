package com.fintech.tradesim.dto;

import com.fintech.tradesim.entity.Account;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountDTO {
    private Long id;
    private String accountNo;
    private BigDecimal cashBalance;
    private BigDecimal frozenCash;
    private BigDecimal availableCash;
    private BigDecimal totalAssets;
    private BigDecimal stockValue;
    private BigDecimal totalProfit;
    private BigDecimal profitRate;
    private Account.AccountStatus status;
}
