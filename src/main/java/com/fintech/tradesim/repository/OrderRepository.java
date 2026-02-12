package com.fintech.tradesim.repository;

import com.fintech.tradesim.entity.Account;
import com.fintech.tradesim.entity.Order;
import com.fintech.tradesim.entity.Security;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNo(String orderNo);

    Page<Order> findByAccountOrderByCreatedAtDesc(Account account, Pageable pageable);

    List<Order> findByAccountAndStatusIn(Account account, List<Order.OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.status IN ('PENDING', 'PARTIAL') AND o.security = :security")
    List<Order> findActiveOrdersBySecurity(Security security);

    @Query("SELECT o FROM Order o WHERE o.status IN ('PENDING', 'PARTIAL')")
    List<Order> findAllActiveOrders();

    List<Order> findByAccountAndSecurityAndStatusIn(Account account, Security security, List<Order.OrderStatus> statuses);
}
