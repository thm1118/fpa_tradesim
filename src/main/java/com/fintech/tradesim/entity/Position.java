package com.fintech.tradesim.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "positions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"account_id", "security_id"})
})
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "security_id", nullable = false)
    private Security security;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(nullable = false)
    private Integer frozenQuantity = 0;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal avgCost = BigDecimal.ZERO;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal marketValue = BigDecimal.ZERO;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal unrealizedProfit = BigDecimal.ZERO;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal profitRate = BigDecimal.ZERO;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Integer getAvailableQuantity() {
        return quantity - frozenQuantity;
    }

    public void updateMarketValue(BigDecimal currentPrice) {
        this.marketValue = currentPrice.multiply(new BigDecimal(quantity));
        this.unrealizedProfit = this.marketValue.subtract(this.totalCost);
        if (this.totalCost.compareTo(BigDecimal.ZERO) > 0) {
            this.profitRate = this.unrealizedProfit.divide(this.totalCost, 4, java.math.RoundingMode.HALF_UP);
        }
    }
}
