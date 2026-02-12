package com.fintech.tradesim.service;

import com.fintech.tradesim.entity.MarketData;
import com.fintech.tradesim.entity.Security;
import com.fintech.tradesim.repository.MarketDataRepository;
import com.fintech.tradesim.repository.SecurityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketSimulationService {
    private final SecurityRepository securityRepository;
    private final MarketDataRepository marketDataRepository;
    private final PositionService positionService;

    private final Random random = new Random();

    @Scheduled(fixedRate = 5000) // Every 5 seconds
    @Transactional
    public void simulateMarketMovement() {
        List<Security> securities = securityRepository.findByTradableTrue();

        for (Security security : securities) {
            updateSecurityPrice(security);
        }
    }

    private void updateSecurityPrice(Security security) {
        BigDecimal currentPrice = security.getCurrentPrice();

        // Random price movement: -2% to +2%
        double changePercent = (random.nextDouble() - 0.5) * 0.04;
        BigDecimal priceChange = currentPrice.multiply(new BigDecimal(changePercent))
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal newPrice = currentPrice.add(priceChange);

        // Ensure price doesn't go below 0.01
        if (newPrice.compareTo(new BigDecimal("0.01")) < 0) {
            newPrice = new BigDecimal("0.01");
        }

        // Update high/low for the day
        if (newPrice.compareTo(security.getHighPrice()) > 0) {
            security.setHighPrice(newPrice);
        }
        if (newPrice.compareTo(security.getLowPrice()) < 0) {
            security.setLowPrice(newPrice);
        }

        // Calculate change from previous close
        BigDecimal dayChange = newPrice.subtract(security.getPreviousClose());
        BigDecimal dayChangePercent = BigDecimal.ZERO;
        if (security.getPreviousClose().compareTo(BigDecimal.ZERO) > 0) {
            dayChangePercent = dayChange.divide(security.getPreviousClose(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        // Simulate volume
        long volumeChange = random.nextInt(10000);
        security.setVolume(security.getVolume() + volumeChange);

        security.setCurrentPrice(newPrice);
        security.setChangePercent(dayChangePercent);
        securityRepository.save(security);

        // Update all positions with this security
        positionService.updateAllPositionMarketValues(security);

        // Record market data
        MarketData marketData = new MarketData();
        marketData.setSymbol(security.getSymbol());
        marketData.setPrice(newPrice);
        marketData.setOpen(security.getOpenPrice());
        marketData.setHigh(security.getHighPrice());
        marketData.setLow(security.getLowPrice());
        marketData.setClose(newPrice);
        marketData.setVolume(security.getVolume());
        marketData.setTimestamp(LocalDateTime.now());
        marketDataRepository.save(marketData);
    }

    @Scheduled(cron = "0 0 9 * * MON-FRI") // 9 AM on weekdays - market open
    @Transactional
    public void resetDailyData() {
        List<Security> securities = securityRepository.findAll();
        for (Security security : securities) {
            security.setPreviousClose(security.getCurrentPrice());
            security.setOpenPrice(security.getCurrentPrice());
            security.setHighPrice(security.getCurrentPrice());
            security.setLowPrice(security.getCurrentPrice());
            security.setVolume(0L);
            security.setChangePercent(BigDecimal.ZERO);
            securityRepository.save(security);
        }
        log.info("Daily market data reset completed");
    }

    @Scheduled(cron = "0 0 0 * * *") // Midnight - cleanup old data
    @Transactional
    public void cleanupOldMarketData() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        marketDataRepository.deleteByTimestampBefore(cutoff);
        log.info("Old market data cleanup completed");
    }
}
