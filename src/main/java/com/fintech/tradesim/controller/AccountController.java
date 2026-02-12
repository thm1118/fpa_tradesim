package com.fintech.tradesim.controller;

import com.fintech.tradesim.client.QuickPayClient;
import com.fintech.tradesim.dto.AccountDTO;
import com.fintech.tradesim.entity.Account;
import com.fintech.tradesim.security.CurrentUser;
import com.fintech.tradesim.security.UserPrincipal;
import com.fintech.tradesim.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final QuickPayClient quickPayClient;

    @GetMapping
    public ResponseEntity<AccountDTO> getMyAccount(@CurrentUser UserPrincipal principal) {
        Account account = accountService.getAccountByUser(principal.getUser());
        return ResponseEntity.ok(accountService.convertToDTO(account));
    }

    /**
     * 将交易盈利提现到QuickPay支付账户
     * 请求体: { "quickpayAccountNo": "QP...", "amount": 1000.00 }
     */
    @PostMapping("/withdraw-to-quickpay")
    public ResponseEntity<?> withdrawToQuickPay(
            @CurrentUser UserPrincipal principal,
            @RequestBody Map<String, Object> request) {

        String quickpayAccountNo = (String) request.get("quickpayAccountNo");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        Account account = accountService.getAccountByUser(principal.getUser());

        // 检查可用现金余额
        if (account.getAvailableCash().compareTo(amount) < 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Insufficient cash balance"
            ));
        }

        // 从TradeSim账户扣减现金
        accountService.subtractCash(account, amount);

        // 调用QuickPay充值接口
        Map<String, Object> result = quickPayClient.recharge(quickpayAccountNo, amount);

        if (!Boolean.TRUE.equals(result.get("success"))) {
            // QuickPay充值失败，回滚TradeSim扣款
            accountService.addCash(account, amount);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "QuickPay recharge failed, amount has been refunded"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully withdrew to QuickPay account",
                "quickpayAccountNo", quickpayAccountNo,
                "amount", amount,
                "remainingCash", account.getCashBalance()
        ));
    }
}
