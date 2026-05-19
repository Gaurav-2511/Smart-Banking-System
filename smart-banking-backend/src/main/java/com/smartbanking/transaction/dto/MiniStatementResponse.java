package com.smartbanking.transaction.dto;

import com.smartbanking.account.entity.AccountStatus;
import com.smartbanking.account.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MiniStatementResponse {

    private String bankName;

    private String userName;
    private String userEmail;

    private Long accountId;
    private String accountNumber;
    private AccountType accountType;
    private AccountStatus status;
    private BigDecimal currentBalance;

    private LocalDateTime generatedAt;

    private long totalTransactions;

    private List<TransactionResponse> transactions;
}