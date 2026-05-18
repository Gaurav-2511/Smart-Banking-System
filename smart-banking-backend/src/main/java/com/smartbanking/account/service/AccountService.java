package com.smartbanking.account.service;

import com.smartbanking.account.dto.AccountCreateRequest;
import com.smartbanking.account.dto.AccountResponse;
import com.smartbanking.account.dto.BalanceResponse;

import java.util.List;

public interface AccountService {

    AccountResponse createAccount(AccountCreateRequest request, String userEmail);

    List<AccountResponse> getMyAccounts(String userEmail);

    BalanceResponse getAccountBalance(Long accountId, String userEmail);
}