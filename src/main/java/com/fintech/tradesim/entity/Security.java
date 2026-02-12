package com.fintech.tradesim.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "securities")
public class Security {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 10)
    private String symbol;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SecurityType type;

    @Column(length = 50)
    private String exchange;

    @Column(length = 50)
    private String sector;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal currentPrice = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal previousClose = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal openPrice = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal highPrice = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal lowPrice = BigDecimal.ZERO;

    @Column(nullable = false)
    private Long volume = 0L;

    @Column(precision = 8, scale = 4)
    private BigDecimal changePercent = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean tradable = true;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum SecurityType {
        STOCK, ETF, FUND, BOND
    }
}
