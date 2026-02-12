package com.fintech.tradesim.service;

import com.fintech.tradesim.dto.RegisterRequest;
import com.fintech.tradesim.dto.UserDTO;
import com.fintech.tradesim.entity.Account;
import com.fintech.tradesim.entity.User;
import com.fintech.tradesim.exception.ResourceConflictException;
import com.fintech.tradesim.repository.AccountRepository;
import com.fintech.tradesim.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("100000.00"); // 10万模拟资金

    @Transactional
    public UserDTO register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceConflictException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user = userRepository.save(user);

        // Create trading account with initial virtual balance
        Account account = new Account();
        account.setUser(user);
        account.setAccountNo(generateAccountNo());
        account.setCashBalance(INITIAL_BALANCE);
        account.setTotalAssets(INITIAL_BALANCE);
        accountRepository.save(account);

        return convertToDTO(user);
    }

    @Transactional
    public void updateRiskLevel(User user, User.RiskLevel riskLevel) {
        user.setRiskLevel(riskLevel);
        userRepository.save(user);
    }

    public UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRealName(user.getRealName());
        dto.setRiskLevel(user.getRiskLevel());
        dto.setVerified(user.getVerified());
        return dto;
    }

    private String generateAccountNo() {
        return "TS" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
