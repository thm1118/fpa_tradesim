package com.fintech.tradesim.repository;

import com.fintech.tradesim.entity.Account;
import com.fintech.tradesim.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUser(User user);
    Optional<Account> findByAccountNo(String accountNo);
    boolean existsByAccountNo(String accountNo);
}
