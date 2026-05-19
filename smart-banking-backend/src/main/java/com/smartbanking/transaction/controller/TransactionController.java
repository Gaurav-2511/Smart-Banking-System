package com.smartbanking.transaction.controller;

import com.smartbanking.transaction.dto.DepositRequest;
import com.smartbanking.transaction.dto.MiniStatementResponse;
import com.smartbanking.transaction.dto.TransactionResponse;
import com.smartbanking.transaction.dto.TransferRequest;
import com.smartbanking.transaction.dto.WithdrawRequest;
import com.smartbanking.transaction.entity.TransactionType;
import com.smartbanking.transaction.service.TransactionService;
import com.smartbanking.util.ApiResponse;
import com.smartbanking.util.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @Valid @RequestBody DepositRequest request,
            Authentication authentication
    ) {

        String userEmail = authentication.getName();

        TransactionResponse response = transactionService.deposit(request, userEmail);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Amount deposited successfully", response));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            Authentication authentication
    ) {

        String userEmail = authentication.getName();

        TransactionResponse response = transactionService.withdraw(request, userEmail);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Amount withdrawn successfully", response));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication
    ) {

        String userEmail = authentication.getName();

        TransactionResponse response = transactionService.transfer(request, userEmail);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Amount transferred successfully", response));
    }

    /*
     * Logged-in user che all transactions.
     *
     * Optional filters:
     * transactionType = DEPOSIT / WITHDRAW / TRANSFER
     * startDate = yyyy-MM-dd
     * endDate = yyyy-MM-dd
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getMyTransactions(
            @RequestParam(required = false) TransactionType transactionType,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size,

            Authentication authentication
    ) {

        String userEmail = authentication.getName();

        PageResponse<TransactionResponse> response = transactionService.getMyTransactions(
                userEmail,
                transactionType,
                startDate,
                endDate,
                page,
                size
        );

        return ResponseEntity
                .ok(ApiResponse.success("Transactions fetched successfully", response));
    }

    /*
     * Specific account che transactions.
     * User फक्त स्वतःच्या account चे transactions पाहू शकतो.
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getAccountTransactions(
            @PathVariable Long accountId,

            @RequestParam(required = false) TransactionType transactionType,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size,

            Authentication authentication
    ) {

        String userEmail = authentication.getName();

        PageResponse<TransactionResponse> response = transactionService.getAccountTransactions(
                accountId,
                userEmail,
                transactionType,
                startDate,
                endDate,
                page,
                size
        );

        return ResponseEntity
                .ok(ApiResponse.success("Account transactions fetched successfully", response));
    }

    /*
     * Mini statement latest 10 transactions दाखवेल.
     */
    @GetMapping("/account/{accountId}/mini-statement")
    public ResponseEntity<ApiResponse<MiniStatementResponse>> getMiniStatement(
            @PathVariable Long accountId,
            Authentication authentication
    ) {

        String userEmail = authentication.getName();

        MiniStatementResponse response = transactionService.getMiniStatement(accountId, userEmail);

        return ResponseEntity
                .ok(ApiResponse.success("Mini statement fetched successfully", response));
    }
}