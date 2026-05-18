package com.smartbanking.account.dto;

import java.math.BigDecimal;

import com.smartbanking.account.entity.AccountStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceResponse {

	private Long accountId;

	private String accountNumber;

	private BigDecimal balance;

	private AccountStatus status;
}
