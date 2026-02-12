package com.fintech.tradesim.controller;

import com.fintech.tradesim.dto.PositionDTO;
import com.fintech.tradesim.entity.Account;
import com.fintech.tradesim.security.CurrentUser;
import com.fintech.tradesim.security.UserPrincipal;
import com.fintech.tradesim.service.AccountService;
import com.fintech.tradesim.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {
    private final PositionService positionService;
    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<PositionDTO>> getPositions(@CurrentUser UserPrincipal principal) {
        Account account = accountService.getAccountByUser(principal.getUser());
        return ResponseEntity.ok(positionService.getPositions(account));
    }
}
