package com.fintech.tradesim.service;

import com.fintech.tradesim.dto.AccountDTO;
import com.fintech.tradesim.entity.Account;
import com.fintech.tradesim.entity.User;
import com.fintech.tradesim.exception.ResourceNotFoundException;
import com.fintech.tradesim.repository.AccountRepository;
import com.fintech.tradesim.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final PositionRepository positionRepository;

    public Account getAccountByUser(User user) {
        return accountRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    @Transactional
    public void addCash(Account account, BigDecimal amount) {
        account.setCashBalance(account.getCashBalance().add(amount));
        updateTotalAssets(account);
        accountRepository.save(account);
    }

    @Transactional
    public void subtractCash(Account account, BigDecimal amount) {
        if (account.getAvailableCash().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient cash balance");
        }
        account.setCashBalance(account.getCashBalance().subtract(amount));
        updateTotalAssets(account);
        accountRepository.save(account);
    }

    @Transactional
    public void freezeCash(Account account, BigDecimal amount) {
        if (account.getAvailableCash().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient available cash to freeze");
        }
        account.setFrozenCash(account.getFrozenCash().add(amount));
        accountRepository.save(account);
    }

    @Transactional
    public void unfreezeCash(Account account, BigDecimal amount) {
        if (account.getFrozenCash().compareTo(amount) < 0) {
            amount = account.getFrozenCash();
        }
        account.setFrozenCash(account.getFrozenCash().subtract(amount));
        accountRepository.save(account);
    }

    @Transactional
    public void updateTotalAssets(Account account) {
        BigDecimal stockValue = positionRepository.sumMarketValueByAccount(account);
        if (stockValue == null) stockValue = BigDecimal.ZERO;

        BigDecimal totalAssets = account.getCashBalance().add(stockValue);
        BigDecimal initialAssets = new BigDecimal("100000.00");
        BigDecimal totalProfit = totalAssets.subtract(initialAssets);

        account.setTotalAssets(totalAssets);
        account.setTotalProfit(totalProfit);

        if (initialAssets.compareTo(BigDecimal.ZERO) > 0) {
            account.setProfitRate(totalProfit.divide(initialAssets, 4, java.math.RoundingMode.HALF_UP));
        }

        accountRepository.save(account);
    }

    public AccountDTO convertToDTO(Account account) {
        BigDecimal stockValue = positionRepository.sumMarketValueByAccount(account);
        if (stockValue == null) stockValue = BigDecimal.ZERO;

        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setAccountNo(account.getAccountNo());
        dto.setCashBalance(account.getCashBalance());
        dto.setFrozenCash(account.getFrozenCash());
        dto.setAvailableCash(account.getAvailableCash());
        dto.setTotalAssets(account.getTotalAssets());
        dto.setStockValue(stockValue);
        dto.setTotalProfit(account.getTotalProfit());
        dto.setProfitRate(account.getProfitRate());
        dto.setStatus(account.getStatus());
        return dto;
    }
}
