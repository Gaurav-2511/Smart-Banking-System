package com.smartbanking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {

    private long totalUsers;

    private long totalAccounts;
    private long activeAccounts;
    private long frozenAccounts;

    private long totalTransactions;

    private BigDecimal totalDepositedAmount;
    private BigDecimal totalWithdrawnAmount;
}