package com.smartbanking.account.service.impl;

import com.smartbanking.account.dto.AccountCreateRequest;
import com.smartbanking.account.dto.AccountResponse;
import com.smartbanking.account.dto.BalanceResponse;
import com.smartbanking.account.entity.AccountStatus;
import com.smartbanking.account.entity.BankAccount;
import com.smartbanking.account.repository.BankAccountRepository;
import com.smartbanking.account.service.AccountService;
import com.smartbanking.exception.ResourceNotFoundException;
import com.smartbanking.user.entity.User;
import com.smartbanking.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public AccountResponse createAccount(AccountCreateRequest request, String userEmail) {

        /*
         * JWT token madhun email milto.
         * त्या email वरून logged-in user database मधून fetch करतो.
         */
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BankAccount bankAccount = BankAccount.builder()
                .accountNumber(generateUniqueAccountNumber())
                .accountType(request.getAccountType())
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .user(user)
                .build();

        BankAccount savedAccount = bankAccountRepository.save(bankAccount);

        return mapToAccountResponse(savedAccount);
    }

    @Override
    public List<AccountResponse> getMyAccounts(String userEmail) {

        List<BankAccount> accounts = bankAccountRepository.findByUserEmail(userEmail);

        return accounts.stream()
                .map(this::mapToAccountResponse)
                .toList();
    }

    @Override
    public BalanceResponse getAccountBalance(Long accountId, String userEmail) {

        /*
         * findByIdAndUserEmail वापरल्यामुळे user दुसऱ्याचा account access करू शकत नाही.
         */
        BankAccount account = bankAccountRepository.findByIdAndUserEmail(accountId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found or you do not have access to this account"));

        return BalanceResponse.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .status(account.getStatus())
                .build();
    }

    private AccountResponse mapToAccountResponse(BankAccount account) {

        return AccountResponse.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }

    private String generateUniqueAccountNumber() {

        String accountNumber;

        do {
            /*
             * Account number format:
             * SB + 10 digit random number
             * Example: SB8392017456
             */
            long randomNumber = 1000000000L + secureRandom.nextInt(900000000);
            accountNumber = "SB" + randomNumber;

        } while (bankAccountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}