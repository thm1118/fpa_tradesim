package com.fintech.tradesim.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * QuickPay服务客户端 —— 封装对QuickPay内部充值接口的调用
 * 用于将TradeSim交易盈利提现到支付账户
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuickPayClient {

    private final RestTemplate restTemplate;

    @Value("${service.quickpay.url}")
    private String quickpayUrl;

    @Value("${service.internal-key}")
    private String internalKey;

    /**
     * 调用QuickPay内部充值接口，将交易盈利转入支付账户
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> recharge(String accountNo, BigDecimal amount) {
        String url = quickpayUrl + "/internal/payment/recharge";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Service-Key", internalKey);

        Map<String, Object> body = Map.of(
                "accountNo", accountNo,
                "amount", amount
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        log.info("Calling QuickPay recharge: accountNo={}, amount={}", accountNo, amount);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
        return response.getBody();
    }
}
