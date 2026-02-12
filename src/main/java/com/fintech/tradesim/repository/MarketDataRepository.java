package com.fintech.tradesim.repository;

import com.fintech.tradesim.entity.MarketData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MarketDataRepository extends JpaRepository<MarketData, Long> {
    List<MarketData> findBySymbolAndTimestampBetweenOrderByTimestampAsc(
            String symbol, LocalDateTime start, LocalDateTime end);

    @Query("SELECT m FROM MarketData m WHERE m.symbol = :symbol ORDER BY m.timestamp DESC LIMIT 1")
    MarketData findLatestBySymbol(String symbol);

    @Query("SELECT m FROM MarketData m WHERE m.symbol = :symbol ORDER BY m.timestamp DESC")
    List<MarketData> findRecentBySymbol(String symbol);

    void deleteByTimestampBefore(LocalDateTime timestamp);
}
