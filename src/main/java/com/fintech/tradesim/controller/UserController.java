package com.fintech.tradesim.controller;

import com.fintech.tradesim.dto.UserDTO;
import com.fintech.tradesim.entity.User;
import com.fintech.tradesim.security.CurrentUser;
import com.fintech.tradesim.security.UserPrincipal;
import com.fintech.tradesim.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(userService.convertToDTO(principal.getUser()));
    }

    @PutMapping("/risk-level")
    public ResponseEntity<Map<String, String>> updateRiskLevel(
            @CurrentUser UserPrincipal principal,
            @RequestBody Map<String, String> request) {
        User.RiskLevel riskLevel = User.RiskLevel.valueOf(request.get("riskLevel").toUpperCase());
        userService.updateRiskLevel(principal.getUser(), riskLevel);
        return ResponseEntity.ok(Map.of("message", "Risk level updated successfully"));
    }
}
