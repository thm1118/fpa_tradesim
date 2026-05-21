package com.fintech.tradesim.messaging.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.tradesim.entity.Security;
import com.fintech.tradesim.entity.Watchlist;
import com.fintech.tradesim.messaging.event.OrderFilledEvent;
import com.fintech.tradesim.repository.SecurityRepository;
import com.fintech.tradesim.repository.WatchlistRepository;
import com.fintech.tradesim.service.AccountService;
import com.fintech.tradesim.service.PositionService;
import com.fintech.tradesim.entity.Account;
import com.fintech.tradesim.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OrderFilledListener {

    private static final Logger log = LoggerFactory.getLogger(OrderFilledListener.class);

    private final ObjectMapper objectMapper;
    private final SecurityRepository securityRepository;
    private final PositionService positionService;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final WatchlistRepository watchlistRepository;

    public OrderFilledListener(ObjectMapper objectMapper,
                               SecurityRepository securityRepository,
                               PositionService positionService,
                               AccountService accountService,
                               AccountRepository accountRepository,
                               WatchlistRepository watchlistRepository) {
        this.objectMapper = objectMapper;
        this.securityRepository = securityRepository;
        this.positionService = positionService;
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.watchlistRepository = watchlistRepository;
    }

    @KafkaListener(topics = "tradesim.order-filled")
    public void onOrderFilled(String message) {
        OrderFilledEvent event;
        try {
            event = objectMapper.readValue(message, OrderFilledEvent.class);
        } catch (Exception e) {
            log.error("Failed to deserialize order-filled message: {}", e.getMessage(), e);
            return;
        }

        // Runs on the Kafka listener thread — already async from the business transaction
        try {
            recomputePortfolioValues(event);
        } catch (Exception e) {
            log.error("Failed to recompute portfolio values for account {}: {}",
                    event.getAccountId(), e.getMessage(), e);
        }

        try {
            checkWatchlistAlerts(event);
        } catch (Exception e) {
            log.error("Failed to check watchlist alerts for symbol {}: {}",
                    event.getSymbol(), e.getMessage(), e);
        }
    }

    private void recomputePortfolioValues(OrderFilledEvent event) {
        // Update position market values for the traded security so they reflect the fill price
        Optional<Security> securityOpt = securityRepository.findBySymbol(event.getSymbol());
        if (securityOpt.isEmpty()) {
            log.warn("Security not found for symbol {} — skipping portfolio recompute", event.getSymbol());
            return;
        }
        Security security = securityOpt.get();

        // Refresh market values for all positions in this security using its current price
        positionService.updateAllPositionMarketValues(security);

        // Recompute total assets for the account that placed the filled order
        Optional<Account> accountOpt = accountRepository.findById(event.getAccountId());
        if (accountOpt.isEmpty()) {
            log.warn("Account {} not found — skipping total assets recompute", event.getAccountId());
            return;
        }
        accountService.updateTotalAssets(accountOpt.get());

        log.debug("Recomputed portfolio values for account {} after fill of order {}",
                event.getAccountId(), event.getOrderId());
    }

    private void checkWatchlistAlerts(OrderFilledEvent event) {
        Optional<Security> securityOpt = securityRepository.findBySymbol(event.getSymbol());
        if (securityOpt.isEmpty()) {
            return;
        }
        Security security = securityOpt.get();

        List<Watchlist> watchers = watchlistRepository.findBySecurity(security);
        if (watchers.isEmpty()) {
            return;
        }

        for (Watchlist watchlist : watchers) {
            log.info("Price alert: symbol={} side={} fillPrice={} qty={} — watched by user={}",
                    event.getSymbol(),
                    event.getSide(),
                    event.getPrice(),
                    event.getQuantity(),
                    watchlist.getUser().getId());
        }
    }
}
