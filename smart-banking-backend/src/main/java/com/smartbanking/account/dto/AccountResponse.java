package com.smartbanking.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.smartbanking.account.entity.AccountStatus;
import com.smartbanking.account.entity.AccountType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {

	private Long accountId;
	
	private String accountNumber;
	
	private AccountType accountType;
	
	private BigDecimal balance;
	
	private AccountStatus status;
	
	private LocalDateTime createdAt;
	
}
