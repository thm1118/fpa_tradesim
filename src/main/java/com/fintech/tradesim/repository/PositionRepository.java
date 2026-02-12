package com.fintech.tradesim.repository;

import com.fintech.tradesim.entity.Account;
import com.fintech.tradesim.entity.Position;
import com.fintech.tradesim.entity.Security;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByAccountAndQuantityGreaterThan(Account account, Integer minQuantity);

    Optional<Position> findByAccountAndSecurity(Account account, Security security);

    @Query("SELECT SUM(p.marketValue) FROM Position p WHERE p.account = :account")
    BigDecimal sumMarketValueByAccount(Account account);

    @Query("SELECT SUM(p.unrealizedProfit) FROM Position p WHERE p.account = :account")
    BigDecimal sumUnrealizedProfitByAccount(Account account);

    List<Position> findBySecurityAndQuantityGreaterThan(Security security, Integer minQuantity);
}
