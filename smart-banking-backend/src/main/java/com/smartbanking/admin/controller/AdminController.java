package com.smartbanking.admin.controller;

import com.smartbanking.admin.dto.AdminAccountResponse;
import com.smartbanking.admin.dto.AdminDashboardResponse;
import com.smartbanking.admin.dto.AdminTransactionResponse;
import com.smartbanking.admin.dto.AdminUserResponse;
import com.smartbanking.admin.service.AdminService;
import com.smartbanking.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /*
     * Admin can view all users.
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> getAllUsers() {

        List<AdminUserResponse> response = adminService.getAllUsers();

        return ResponseEntity
                .ok(ApiResponse.success("Users fetched successfully", response));
    }

    /*
     * Admin can view all bank accounts.
     */
    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<AdminAccountResponse>>> getAllAccounts() {

        List<AdminAccountResponse> response = adminService.getAllAccounts();

        return ResponseEntity
                .ok(ApiResponse.success("Accounts fetched successfully", response));
    }

    /*
     * Admin can freeze account.
     */
    @PutMapping("/accounts/{accountId}/freeze")
    public ResponseEntity<ApiResponse<AdminAccountResponse>> freezeAccount(
            @PathVariable Long accountId
    ) {

        AdminAccountResponse response = adminService.freezeAccount(accountId);

        return ResponseEntity
                .ok(ApiResponse.success("Account frozen successfully", response));
    }

    /*
     * Admin can activate frozen account.
     */
    @PutMapping("/accounts/{accountId}/activate")
    public ResponseEntity<ApiResponse<AdminAccountResponse>> activateAccount(
            @PathVariable Long accountId
    ) {

        AdminAccountResponse response = adminService.activateAccount(accountId);

        return ResponseEntity
                .ok(ApiResponse.success("Account activated successfully", response));
    }

    /*
     * Admin can view all transactions.
     */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<AdminTransactionResponse>>> getAllTransactions() {

        List<AdminTransactionResponse> response = adminService.getAllTransactions();

        return ResponseEntity
                .ok(ApiResponse.success("Transactions fetched successfully", response));
    }

    /*
     * Admin dashboard stats.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboardStats() {

        AdminDashboardResponse response = adminService.getDashboardStats();

        return ResponseEntity
                .ok(ApiResponse.success("Admin dashboard stats fetched successfully", response));
    }
}