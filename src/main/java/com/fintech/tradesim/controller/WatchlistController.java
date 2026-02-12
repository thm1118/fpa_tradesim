package com.fintech.tradesim.controller;

import com.fintech.tradesim.dto.SecurityDTO;
import com.fintech.tradesim.security.CurrentUser;
import com.fintech.tradesim.security.UserPrincipal;
import com.fintech.tradesim.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {
    private final WatchlistService watchlistService;

    @GetMapping
    public ResponseEntity<List<SecurityDTO>> getWatchlist(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(watchlistService.getWatchlist(principal.getUser()));
    }

    @PostMapping("/{symbol}")
    public ResponseEntity<Map<String, String>> addToWatchlist(
            @CurrentUser UserPrincipal principal,
            @PathVariable String symbol) {
        watchlistService.addToWatchlist(principal.getUser(), symbol);
        return ResponseEntity.ok(Map.of("message", "Added to watchlist"));
    }

    @DeleteMapping("/{symbol}")
    public ResponseEntity<Map<String, String>> removeFromWatchlist(
            @CurrentUser UserPrincipal principal,
            @PathVariable String symbol) {
        watchlistService.removeFromWatchlist(principal.getUser(), symbol);
        return ResponseEntity.ok(Map.of("message", "Removed from watchlist"));
    }

    @GetMapping("/{symbol}/exists")
    public ResponseEntity<Map<String, Boolean>> isInWatchlist(
            @CurrentUser UserPrincipal principal,
            @PathVariable String symbol) {
        boolean exists = watchlistService.isInWatchlist(principal.getUser(), symbol);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}
