package com.fintech.tradesim.repository;

import com.fintech.tradesim.entity.Account;
import com.fintech.tradesim.entity.Order;
import com.fintech.tradesim.entity.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    Page<Trade> findByAccountOrderByExecutedAtDesc(Account account, Pageable pageable);

    List<Trade> findByOrder(Order order);

    List<Trade> findByAccountAndExecutedAtBetween(Account account, LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(t.commission) FROM Trade t WHERE t.account = :account AND t.executedAt >= :start")
    BigDecimal sumCommissionByAccountAndPeriod(Account account, LocalDateTime start);

    @Query("SELECT SUM(t.amount) FROM Trade t WHERE t.account = :account AND t.side = 'BUY' AND t.executedAt >= :start")
    BigDecimal sumBuyAmountByAccountAndPeriod(Account account, LocalDateTime start);

    @Query("SELECT SUM(t.amount) FROM Trade t WHERE t.account = :account AND t.side = 'SELL' AND t.executedAt >= :start")
    BigDecimal sumSellAmountByAccountAndPeriod(Account account, LocalDateTime start);
}
