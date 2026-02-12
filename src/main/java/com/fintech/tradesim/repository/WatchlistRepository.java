package com.fintech.tradesim.repository;

import com.fintech.tradesim.entity.Security;
import com.fintech.tradesim.entity.User;
import com.fintech.tradesim.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    List<Watchlist> findByUserOrderByAddedAtDesc(User user);

    Optional<Watchlist> findByUserAndSecurity(User user, Security security);

    boolean existsByUserAndSecurity(User user, Security security);

    void deleteByUserAndSecurity(User user, Security security);

    long countByUser(User user);
}
