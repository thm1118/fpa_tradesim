package com.fintech.tradesim.schedule;

import com.fintech.tradesim.entity.Account;
import com.fintech.tradesim.entity.Security;
import com.fintech.tradesim.repository.AccountRepository;
import com.fintech.tradesim.repository.SecurityRepository;
import com.fintech.tradesim.service.AccountService;
import com.fintech.tradesim.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收盘结算任务 — 每个交易日 15:00 执行按市值计价（mark-to-market）结算。
 * 对所有证券重新计算持仓市值，然后对所有账户重新计算总资产、总盈亏及收益率。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduledTask {

    private final SecurityRepository securityRepository;
    private final AccountRepository accountRepository;
    private final PositionService positionService;
    private final AccountService accountService;

    /**
     * 收盘结算：周一至周五 15:00 触发。
     * 步骤一：遍历全部可交易证券，更新各证券对应的所有持仓市值。
     * 步骤二：遍历全部账户，重新计算总资产、总盈亏及收益率并持久化。
     */
    @Scheduled(cron = "0 0 15 * * MON-FRI")
    @Transactional
    public void settleAtMarketClose() {
        log.info("收盘结算开始 — 按当前市价重新计算持仓市值及账户资产");

        // Step 1: mark-to-market — refresh position market values for every tradable security
        List<Security> securities = securityRepository.findByTradableTrue();
        for (Security security : securities) {
            positionService.updateAllPositionMarketValues(security);
        }

        // Step 2: recompute total assets / total profit / profit rate for every account
        List<Account> accounts = accountRepository.findAll();
        for (Account account : accounts) {
            accountService.updateTotalAssets(account);
        }

        log.info("收盘结算完成 — 共结算账户数: {}", accounts.size());
    }
}
