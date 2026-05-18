package com.smartbanking.account.dto;

import com.smartbanking.account.entity.AccountType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountCreateRequest {

	@NotNull(message = "Account type is required")
	private AccountType accountType;
}
