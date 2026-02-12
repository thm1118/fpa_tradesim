package com.fintech.tradesim.service;

import com.fintech.tradesim.dto.PositionDTO;
import com.fintech.tradesim.entity.Account;
import com.fintech.tradesim.entity.Position;
import com.fintech.tradesim.entity.Security;
import com.fintech.tradesim.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionService {
    private final PositionRepository positionRepository;

    public List<PositionDTO> getPositions(Account account) {
        return positionRepository.findByAccountAndQuantityGreaterThan(account, 0).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<Position> getPosition(Account account, Security security) {
        return positionRepository.findByAccountAndSecurity(account, security);
    }

    @Transactional
    public Position addPosition(Account account, Security security, int quantity, BigDecimal price) {
        Position position = positionRepository.findByAccountAndSecurity(account, security)
                .orElseGet(() -> {
                    Position p = new Position();
                    p.setAccount(account);
                    p.setSecurity(security);
                    return p;
                });

        BigDecimal newCost = price.multiply(new BigDecimal(quantity));
        BigDecimal totalCost = position.getTotalCost().add(newCost);
        int totalQuantity = position.getQuantity() + quantity;

        position.setQuantity(totalQuantity);
        position.setTotalCost(totalCost);
        position.setAvgCost(totalCost.divide(new BigDecimal(totalQuantity), 4, RoundingMode.HALF_UP));
        position.updateMarketValue(security.getCurrentPrice());

        return positionRepository.save(position);
    }

    @Transactional
    public Position reducePosition(Account account, Security security, int quantity, BigDecimal price) {
        Position position = positionRepository.findByAccountAndSecurity(account, security)
                .orElseThrow(() -> new IllegalStateException("No position found for " + security.getSymbol()));

        if (position.getAvailableQuantity() < quantity) {
            throw new IllegalStateException("Insufficient position quantity");
        }

        int newQuantity = position.getQuantity() - quantity;
        BigDecimal costReduction = position.getAvgCost().multiply(new BigDecimal(quantity));

        position.setQuantity(newQuantity);
        position.setTotalCost(position.getTotalCost().subtract(costReduction));

        if (newQuantity > 0) {
            position.updateMarketValue(security.getCurrentPrice());
        } else {
            position.setMarketValue(BigDecimal.ZERO);
            position.setUnrealizedProfit(BigDecimal.ZERO);
            position.setProfitRate(BigDecimal.ZERO);
        }

        return positionRepository.save(position);
    }

    @Transactional
    public void freezePosition(Position position, int quantity) {
        if (position.getAvailableQuantity() < quantity) {
            throw new IllegalStateException("Insufficient available quantity to freeze");
        }
        position.setFrozenQuantity(position.getFrozenQuantity() + quantity);
        positionRepository.save(position);
    }

    @Transactional
    public void unfreezePosition(Position position, int quantity) {
        if (position.getFrozenQuantity() < quantity) {
            quantity = position.getFrozenQuantity();
        }
        position.setFrozenQuantity(position.getFrozenQuantity() - quantity);
        positionRepository.save(position);
    }

    @Transactional
    public void updateAllPositionMarketValues(Security security) {
        List<Position> positions = positionRepository.findBySecurityAndQuantityGreaterThan(security, 0);
        for (Position position : positions) {
            position.updateMarketValue(security.getCurrentPrice());
            positionRepository.save(position);
        }
    }

    public PositionDTO convertToDTO(Position position) {
        PositionDTO dto = new PositionDTO();
        dto.setId(position.getId());
        dto.setSymbol(position.getSecurity().getSymbol());
        dto.setSecurityName(position.getSecurity().getName());
        dto.setQuantity(position.getQuantity());
        dto.setAvailableQuantity(position.getAvailableQuantity());
        dto.setFrozenQuantity(position.getFrozenQuantity());
        dto.setAvgCost(position.getAvgCost());
        dto.setCurrentPrice(position.getSecurity().getCurrentPrice());
        dto.setTotalCost(position.getTotalCost());
        dto.setMarketValue(position.getMarketValue());
        dto.setUnrealizedProfit(position.getUnrealizedProfit());
        dto.setProfitRate(position.getProfitRate());
        return dto;
    }
}
