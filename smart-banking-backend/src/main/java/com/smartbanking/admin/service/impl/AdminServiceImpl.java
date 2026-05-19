package com.smartbanking.admin.service.impl;

import com.smartbanking.account.entity.AccountStatus;
import com.smartbanking.account.entity.BankAccount;
import com.smartbanking.account.repository.BankAccountRepository;
import com.smartbanking.admin.dto.AdminAccountResponse;
import com.smartbanking.admin.dto.AdminDashboardResponse;
import com.smartbanking.admin.dto.AdminTransactionResponse;
import com.smartbanking.admin.dto.AdminUserResponse;
import com.smartbanking.admin.service.AdminService;
import com.smartbanking.exception.BadRequestException;
import com.smartbanking.exception.ResourceNotFoundException;
import com.smartbanking.transaction.entity.Transaction;
import com.smartbanking.transaction.entity.TransactionStatus;
import com.smartbanking.transaction.entity.TransactionType;
import com.smartbanking.transaction.repository.TransactionRepository;
import com.smartbanking.user.entity.User;
import com.smartbanking.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {

        List<User> users = userRepository.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return users.stream()
                .map(this::mapToAdminUserResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminAccountResponse> getAllAccounts() {

        List<BankAccount> accounts = bankAccountRepository.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return accounts.stream()
                .map(this::mapToAdminAccountResponse)
                .toList();
    }

    @Override
    @Transactional
    public AdminAccountResponse freezeAccount(Long accountId) {

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BadRequestException("Closed account cannot be frozen");
        }

        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new BadRequestException("Account is already frozen");
        }

        account.setStatus(AccountStatus.FROZEN);

        BankAccount savedAccount = bankAccountRepository.save(account);

        return mapToAdminAccountResponse(savedAccount);
    }

    @Override
    @Transactional
    public AdminAccountResponse activateAccount(Long accountId) {

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BadRequestException("Closed account cannot be activated");
        }

        if (account.getStatus() == AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is already active");
        }

        account.setStatus(AccountStatus.ACTIVE);

        BankAccount savedAccount = bankAccountRepository.save(account);

        return mapToAdminAccountResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminTransactionResponse> getAllTransactions() {

        List<Transaction> transactions = transactionRepository.findAllByOrderByCreatedAtDesc();

        return transactions.stream()
                .map(this::mapToAdminTransactionResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStats() {

        long totalUsers = userRepository.count();

        long totalAccounts = bankAccountRepository.count();
        long activeAccounts = bankAccountRepository.countByStatus(AccountStatus.ACTIVE);
        long frozenAccounts = bankAccountRepository.countByStatus(AccountStatus.FROZEN);

        long totalTransactions = transactionRepository.count();

        BigDecimal totalDepositedAmount = transactionRepository.getTotalAmountByTypeAndStatus(
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS
        );

        BigDecimal totalWithdrawnAmount = transactionRepository.getTotalAmountByTypeAndStatus(
                TransactionType.WITHDRAW,
                TransactionStatus.SUCCESS
        );

        if (totalDepositedAmount == null) {
            totalDepositedAmount = BigDecimal.ZERO;
        }

        if (totalWithdrawnAmount == null) {
            totalWithdrawnAmount = BigDecimal.ZERO;
        }

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalAccounts(totalAccounts)
                .activeAccounts(activeAccounts)
                .frozenAccounts(frozenAccounts)
                .totalTransactions(totalTransactions)
                .totalDepositedAmount(totalDepositedAmount)
                .totalWithdrawnAmount(totalWithdrawnAmount)
                .build();
    }

    private AdminUserResponse mapToAdminUserResponse(User user) {

        return AdminUserResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .totalAccounts(user.getBankAccounts() == null ? 0 : user.getBankAccounts().size())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AdminAccountResponse mapToAdminAccountResponse(BankAccount account) {

        User user = account.getUser();

        return AdminAccountResponse.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .status(account.getStatus())
                .userId(user.getId())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .createdAt(account.getCreatedAt())
                .build();
    }

    private AdminTransactionResponse mapToAdminTransactionResponse(Transaction transaction) {

        BankAccount account = transaction.getBankAccount();
        User user = account.getUser();

        return AdminTransactionResponse.builder()
                .transactionId(transaction.getId())
                .referenceNumber(transaction.getReferenceNumber())
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .userId(user.getId())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .amount(transaction.getAmount())
                .fromAccountNumber(transaction.getFromAccountNumber())
                .toAccountNumber(transaction.getToAccountNumber())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}