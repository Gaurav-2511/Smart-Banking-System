package com.smartbanking.admin.service;

import com.smartbanking.admin.dto.AdminAccountResponse;
import com.smartbanking.admin.dto.AdminDashboardResponse;
import com.smartbanking.admin.dto.AdminTransactionResponse;
import com.smartbanking.admin.dto.AdminUserResponse;

import java.util.List;

public interface AdminService {

    List<AdminUserResponse> getAllUsers();

    List<AdminAccountResponse> getAllAccounts();

    AdminAccountResponse freezeAccount(Long accountId);

    AdminAccountResponse activateAccount(Long accountId);

    List<AdminTransactionResponse> getAllTransactions();

    AdminDashboardResponse getDashboardStats();
}