package com.smartbanking.admin.dto;

import com.smartbanking.account.entity.AccountStatus;
import com.smartbanking.account.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAccountResponse {

    private Long accountId;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountStatus status;

    private Long userId;
    private String userName;
    private String userEmail;

    private LocalDateTime createdAt;
}