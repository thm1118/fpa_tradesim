package com.fintech.tradesim.service;

import com.fintech.tradesim.dto.OrderDTO;
import com.fintech.tradesim.dto.OrderRequest;
import com.fintech.tradesim.dto.TradeDTO;
import com.fintech.tradesim.entity.*;
import com.fintech.tradesim.exception.TradingException;
import com.fintech.tradesim.repository.OrderRepository;
import com.fintech.tradesim.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TradingService {
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final AccountService accountService;
    private final SecurityService securityService;
    private final PositionService positionService;

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.0003"); // 0.03%
    private static final BigDecimal MIN_COMMISSION = new BigDecimal("5.00");

    @Transactional
    public OrderDTO placeOrder(User user, OrderRequest request) {
        Account account = accountService.getAccountByUser(user);
        Security security = securityService.getBySymbol(request.getSymbol());

        if (!security.getTradable()) {
            throw new TradingException("Security is not tradable: " + security.getSymbol());
        }

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new TradingException("Account is not active");
        }

        // Validate order
        validateOrder(account, security, request);

        // Create order
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setAccount(account);
        order.setSecurity(security);
        order.setSide(request.getSide());
        order.setType(request.getType());
        order.setQuantity(request.getQuantity());
        order.setLimitPrice(request.getLimitPrice());
        order.setStopPrice(request.getStopPrice());
        order.setStatus(Order.OrderStatus.PENDING);

        // For buy orders, freeze cash
        if (request.getSide() == Order.OrderSide.BUY) {
            BigDecimal estimatedCost = getEstimatedCost(security, request);
            accountService.freezeCash(account, estimatedCost);
        }

        // For sell orders, freeze position
        if (request.getSide() == Order.OrderSide.SELL) {
            Optional<Position> positionOpt = positionService.getPosition(account, security);
            if (positionOpt.isEmpty() || positionOpt.get().getAvailableQuantity() < request.getQuantity()) {
                throw new TradingException("Insufficient position quantity");
            }
            positionService.freezePosition(positionOpt.get(), request.getQuantity());
        }

        order = orderRepository.save(order);

        // Try to execute market orders immediately
        if (request.getType() == Order.OrderType.MARKET) {
            executeOrder(order, security.getCurrentPrice());
        }

        return convertToOrderDTO(order);
    }

    @Transactional
    public OrderDTO cancelOrder(User user, String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new TradingException("Order not found: " + orderNo));

        if (!order.getAccount().getUser().getId().equals(user.getId())) {
            throw new TradingException("Not authorized to cancel this order");
        }

        if (order.getStatus() != Order.OrderStatus.PENDING && order.getStatus() != Order.OrderStatus.PARTIAL) {
            throw new TradingException("Cannot cancel order with status: " + order.getStatus());
        }

        // Unfreeze resources
        if (order.getSide() == Order.OrderSide.BUY) {
            BigDecimal frozenAmount = getEstimatedCost(order.getSecurity(),
                    createOrderRequest(order)).multiply(
                    new BigDecimal(order.getRemainingQuantity()))
                    .divide(new BigDecimal(order.getQuantity()), 2, RoundingMode.HALF_UP);
            accountService.unfreezeCash(order.getAccount(), frozenAmount);
        } else {
            Optional<Position> positionOpt = positionService.getPosition(order.getAccount(), order.getSecurity());
            positionOpt.ifPresent(p -> positionService.unfreezePosition(p, order.getRemainingQuantity()));
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelReason("Cancelled by user");
        orderRepository.save(order);

        return convertToOrderDTO(order);
    }

    @Transactional
    public void executeOrder(Order order, BigDecimal price) {
        Account account = order.getAccount();
        Security security = order.getSecurity();
        int quantity = order.getRemainingQuantity();

        BigDecimal amount = price.multiply(new BigDecimal(quantity));
        BigDecimal commission = calculateCommission(amount);

        // Create trade record
        Trade trade = new Trade();
        trade.setTradeNo(generateTradeNo());
        trade.setOrder(order);
        trade.setAccount(account);
        trade.setSecurity(security);
        trade.setSide(order.getSide());
        trade.setQuantity(quantity);
        trade.setPrice(price);
        trade.setAmount(amount);
        trade.setCommission(commission);
        tradeRepository.save(trade);

        // Update order
        order.setFilledQuantity(order.getFilledQuantity() + quantity);
        order.setAvgFillPrice(price);
        order.setCommission(order.getCommission().add(commission));
        order.setStatus(Order.OrderStatus.FILLED);
        order.setFilledAt(LocalDateTime.now());
        orderRepository.save(order);

        // Update account and position
        if (order.getSide() == Order.OrderSide.BUY) {
            BigDecimal totalCost = amount.add(commission);
            accountService.unfreezeCash(account, getEstimatedCost(security, createOrderRequest(order)));
            accountService.subtractCash(account, totalCost);
            positionService.addPosition(account, security, quantity, price);
        } else {
            BigDecimal netProceeds = amount.subtract(commission);
            Optional<Position> positionOpt = positionService.getPosition(account, security);
            positionOpt.ifPresent(p -> positionService.unfreezePosition(p, quantity));
            positionService.reducePosition(account, security, quantity, price);
            accountService.addCash(account, netProceeds);
        }

        // Update account total assets
        accountService.updateTotalAssets(account);
    }

    public Page<OrderDTO> getOrders(User user, Pageable pageable) {
        Account account = accountService.getAccountByUser(user);
        return orderRepository.findByAccountOrderByCreatedAtDesc(account, pageable)
                .map(this::convertToOrderDTO);
    }

    public List<OrderDTO> getActiveOrders(User user) {
        Account account = accountService.getAccountByUser(user);
        List<Order.OrderStatus> activeStatuses = Arrays.asList(Order.OrderStatus.PENDING, Order.OrderStatus.PARTIAL);
        return orderRepository.findByAccountAndStatusIn(account, activeStatuses).stream()
                .map(this::convertToOrderDTO)
                .toList();
    }

    public Page<TradeDTO> getTrades(User user, Pageable pageable) {
        Account account = accountService.getAccountByUser(user);
        return tradeRepository.findByAccountOrderByExecutedAtDesc(account, pageable)
                .map(this::convertToTradeDTO);
    }

    private void validateOrder(Account account, Security security, OrderRequest request) {
        if (request.getSide() == Order.OrderSide.BUY) {
            BigDecimal estimatedCost = getEstimatedCost(security, request);
            if (account.getAvailableCash().compareTo(estimatedCost) < 0) {
                throw new TradingException("Insufficient cash balance. Required: " + estimatedCost +
                        ", Available: " + account.getAvailableCash());
            }
        } else {
            Optional<Position> positionOpt = positionService.getPosition(account, security);
            if (positionOpt.isEmpty() || positionOpt.get().getAvailableQuantity() < request.getQuantity()) {
                int available = positionOpt.map(Position::getAvailableQuantity).orElse(0);
                throw new TradingException("Insufficient position. Required: " + request.getQuantity() +
                        ", Available: " + available);
            }
        }

        if (request.getType() == Order.OrderType.LIMIT && request.getLimitPrice() == null) {
            throw new TradingException("Limit price is required for limit orders");
        }

        if (request.getType() == Order.OrderType.STOP && request.getStopPrice() == null) {
            throw new TradingException("Stop price is required for stop orders");
        }
    }

    private BigDecimal getEstimatedCost(Security security, OrderRequest request) {
        BigDecimal price = request.getLimitPrice() != null ? request.getLimitPrice() : security.getCurrentPrice();
        BigDecimal amount = price.multiply(new BigDecimal(request.getQuantity()));
        BigDecimal commission = calculateCommission(amount);
        // Add 1% buffer for market orders
        if (request.getType() == Order.OrderType.MARKET) {
            amount = amount.multiply(new BigDecimal("1.01"));
        }
        return amount.add(commission);
    }

    private BigDecimal calculateCommission(BigDecimal amount) {
        BigDecimal commission = amount.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
        return commission.compareTo(MIN_COMMISSION) < 0 ? MIN_COMMISSION : commission;
    }

    private OrderRequest createOrderRequest(Order order) {
        OrderRequest request = new OrderRequest();
        request.setSymbol(order.getSecurity().getSymbol());
        request.setSide(order.getSide());
        request.setType(order.getType());
        request.setQuantity(order.getQuantity());
        request.setLimitPrice(order.getLimitPrice());
        request.setStopPrice(order.getStopPrice());
        return request;
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private String generateTradeNo() {
        return "TRD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    public OrderDTO convertToOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setSymbol(order.getSecurity().getSymbol());
        dto.setSecurityName(order.getSecurity().getName());
        dto.setSide(order.getSide());
        dto.setType(order.getType());
        dto.setQuantity(order.getQuantity());
        dto.setFilledQuantity(order.getFilledQuantity());
        dto.setRemainingQuantity(order.getRemainingQuantity());
        dto.setLimitPrice(order.getLimitPrice());
        dto.setStopPrice(order.getStopPrice());
        dto.setAvgFillPrice(order.getAvgFillPrice());
        dto.setStatus(order.getStatus());
        dto.setCommission(order.getCommission());
        dto.setCancelReason(order.getCancelReason());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setFilledAt(order.getFilledAt());
        return dto;
    }

    public TradeDTO convertToTradeDTO(Trade trade) {
        TradeDTO dto = new TradeDTO();
        dto.setId(trade.getId());
        dto.setTradeNo(trade.getTradeNo());
        dto.setOrderNo(trade.getOrder().getOrderNo());
        dto.setSymbol(trade.getSecurity().getSymbol());
        dto.setSecurityName(trade.getSecurity().getName());
        dto.setSide(trade.getSide());
        dto.setQuantity(trade.getQuantity());
        dto.setPrice(trade.getPrice());
        dto.setAmount(trade.getAmount());
        dto.setCommission(trade.getCommission());
        dto.setExecutedAt(trade.getExecutedAt());
        return dto;
    }
}
