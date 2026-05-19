package com.smartbanking.transaction.service;

import com.smartbanking.transaction.dto.DepositRequest;
import com.smartbanking.transaction.dto.MiniStatementResponse;
import com.smartbanking.transaction.dto.TransactionResponse;
import com.smartbanking.transaction.dto.TransferRequest;
import com.smartbanking.transaction.dto.WithdrawRequest;
import com.smartbanking.transaction.entity.TransactionType;
import com.smartbanking.util.PageResponse;

import java.time.LocalDate;

public interface TransactionService {

    TransactionResponse deposit(DepositRequest request, String userEmail);

    TransactionResponse withdraw(WithdrawRequest request, String userEmail);

    TransactionResponse transfer(TransferRequest request, String userEmail);

    PageResponse<TransactionResponse> getMyTransactions(
            String userEmail,
            TransactionType transactionType,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    );

    PageResponse<TransactionResponse> getAccountTransactions(
            Long accountId,
            String userEmail,
            TransactionType transactionType,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    );

    MiniStatementResponse getMiniStatement(Long accountId, String userEmail);
}