package com.smartbanking.transaction.service;

import com.smartbanking.transaction.dto.DepositRequest;
import com.smartbanking.transaction.dto.TransactionResponse;
import com.smartbanking.transaction.dto.TransferRequest;
import com.smartbanking.transaction.dto.WithdrawRequest;

public interface TransactionService {

    TransactionResponse deposit(DepositRequest request, String userEmail);

    TransactionResponse withdraw(WithdrawRequest request, String userEmail);

    TransactionResponse transfer(TransferRequest request, String userEmail);
}