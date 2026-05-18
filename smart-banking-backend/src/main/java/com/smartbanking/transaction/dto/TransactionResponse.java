package com.smartbanking.transaction.dto;

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
public class TransactionResponse {

    private Long transactionId;
    private String referenceNumber;

    private Long accountId;
    private String accountNumber;

    private TransactionType transactionType;
    private TransactionStatus status;

    private BigDecimal amount;
    private BigDecimal currentBalance;

    private String fromAccountNumber;
    private String toAccountNumber;

    private String description;
    private LocalDateTime createdAt;
}