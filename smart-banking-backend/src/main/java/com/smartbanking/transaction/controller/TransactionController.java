package com.smartbanking.transaction.controller;

import com.smartbanking.transaction.dto.DepositRequest;
import com.smartbanking.transaction.dto.TransactionResponse;
import com.smartbanking.transaction.dto.TransferRequest;
import com.smartbanking.transaction.dto.WithdrawRequest;
import com.smartbanking.transaction.service.TransactionService;
import com.smartbanking.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

	private final TransactionService transactionService;

	@PostMapping("/deposit")
	public ResponseEntity<ApiResponse<TransactionResponse>> deposit(@Valid @RequestBody DepositRequest request,
			Authentication authentication) {

		String userEmail = authentication.getName();

		TransactionResponse response = transactionService.deposit(request, userEmail);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Amount deposited successfully", response));
	}

	@PostMapping("/withdraw")
	public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(@Valid @RequestBody WithdrawRequest request,
			Authentication authentication) {

		String userEmail = authentication.getName();

		TransactionResponse response = transactionService.withdraw(request, userEmail);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Amount withdrawn successfully", response));
	}

	@PostMapping("/transfer")
	public ResponseEntity<ApiResponse<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request,
			Authentication authentication) {

		String userEmail = authentication.getName();

		TransactionResponse response = transactionService.transfer(request, userEmail);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Amount transferred successfully", response));
	}
}