package com.smartbanking.account.controller;

import com.smartbanking.account.dto.AccountCreateRequest;
import com.smartbanking.account.dto.AccountResponse;
import com.smartbanking.account.dto.BalanceResponse;
import com.smartbanking.account.service.AccountService;
import com.smartbanking.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /*
     * Create bank account for logged-in user.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody AccountCreateRequest request,
            Authentication authentication
    ) {

        String userEmail = authentication.getName();

        AccountResponse response = accountService.createAccount(request, userEmail);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bank account created successfully", response));
    }

    /*
     * Get all accounts of logged-in user.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts(
            Authentication authentication
    ) {

        String userEmail = authentication.getName();

        List<AccountResponse> response = accountService.getMyAccounts(userEmail);

        return ResponseEntity
                .ok(ApiResponse.success("Accounts fetched successfully", response));
    }

    /*
     * Get balance of logged-in user's specific account.
     */
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> getAccountBalance(
            @PathVariable Long accountId,
            Authentication authentication
    ) {

        String userEmail = authentication.getName();

        BalanceResponse response = accountService.getAccountBalance(accountId, userEmail);

        return ResponseEntity
                .ok(ApiResponse.success("Account balance fetched successfully", response));
    }
}