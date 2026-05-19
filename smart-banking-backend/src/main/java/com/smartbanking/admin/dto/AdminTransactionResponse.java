package com.smartbanking.admin.dto;

import com.smartbanking.transaction.entity.TransactionStatus;
import com.smartbanking.transaction.entity.TransactionType;
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
public class AdminTransactionResponse {

    private Long transactionId;
    private String referenceNumber;

    private Long accountId;
    private String accountNumber;

    private Long userId;
    private String userName;
    private String userEmail;

    private TransactionType transactionType;
    private TransactionStatus status;

    private BigDecimal amount;
    private String fromAccountNumber;
    private String toAccountNumber;
    private String description;

    private LocalDateTime createdAt;
}