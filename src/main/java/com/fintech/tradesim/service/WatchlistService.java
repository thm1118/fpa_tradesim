package com.fintech.tradesim.service;

import com.fintech.tradesim.dto.SecurityDTO;
import com.fintech.tradesim.entity.Security;
import com.fintech.tradesim.entity.User;
import com.fintech.tradesim.entity.Watchlist;
import com.fintech.tradesim.exception.ResourceConflictException;
import com.fintech.tradesim.exception.ResourceNotFoundException;
import com.fintech.tradesim.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WatchlistService {
    private final WatchlistRepository watchlistRepository;
    private final SecurityService securityService;

    private static final int MAX_WATCHLIST_SIZE = 50;

    public List<SecurityDTO> getWatchlist(User user) {
        return watchlistRepository.findByUserOrderByAddedAtDesc(user).stream()
                .map(w -> securityService.convertToDTO(w.getSecurity()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addToWatchlist(User user, String symbol) {
        Security security = securityService.getBySymbol(symbol);

        if (watchlistRepository.existsByUserAndSecurity(user, security)) {
            throw new ResourceConflictException("Security already in watchlist");
        }

        if (watchlistRepository.countByUser(user) >= MAX_WATCHLIST_SIZE) {
            throw new ResourceConflictException("Watchlist is full (max " + MAX_WATCHLIST_SIZE + " items)");
        }

        Watchlist watchlist = new Watchlist();
        watchlist.setUser(user);
        watchlist.setSecurity(security);
        watchlistRepository.save(watchlist);
    }

    @Transactional
    public void removeFromWatchlist(User user, String symbol) {
        Security security = securityService.getBySymbol(symbol);

        Watchlist watchlist = watchlistRepository.findByUserAndSecurity(user, security)
                .orElseThrow(() -> new ResourceNotFoundException("Security not in watchlist"));

        watchlistRepository.delete(watchlist);
    }

    public boolean isInWatchlist(User user, String symbol) {
        try {
            Security security = securityService.getBySymbol(symbol);
            return watchlistRepository.existsByUserAndSecurity(user, security);
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }
}
