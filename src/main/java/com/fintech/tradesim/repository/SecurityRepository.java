package com.fintech.tradesim.repository;

import com.fintech.tradesim.entity.Security;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityRepository extends JpaRepository<Security, Long> {
    Optional<Security> findBySymbol(String symbol);
    List<Security> findByTradableTrue();
    List<Security> findByType(Security.SecurityType type);
    List<Security> findBySector(String sector);

    @Query("SELECT s FROM Security s WHERE s.tradable = true ORDER BY s.volume DESC")
    List<Security> findTopByVolume();

    @Query("SELECT s FROM Security s WHERE s.tradable = true ORDER BY s.changePercent DESC")
    List<Security> findTopGainers();

    @Query("SELECT s FROM Security s WHERE s.tradable = true ORDER BY s.changePercent ASC")
    List<Security> findTopLosers();

    List<Security> findByNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(String name, String symbol);
}
