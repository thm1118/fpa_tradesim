package com.fintech.tradesim.dto;

import com.fintech.tradesim.entity.User;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String realName;
    private User.RiskLevel riskLevel;
    private Boolean verified;
}
