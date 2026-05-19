package com.smartbanking.account.repository;

import com.smartbanking.account.entity.AccountStatus;
import com.smartbanking.account.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    /*
     * Account number varun account find karaycha.
     */
    Optional<BankAccount> findByAccountNumber(String accountNumber);

    /*
     * Account number duplicate aahe ka check karaycha.
     */
    boolean existsByAccountNumber(String accountNumber);

    /*
     * Logged-in user che all accounts fetch karayche.
     */
    List<BankAccount> findByUserId(Long userId);

    /*
     * Logged-in user email varun accounts fetch karayche.
     */
    List<BankAccount> findByUserEmail(String email);

    /*
     * Specific account हा logged-in user चाच आहे का check karaycha.
     */
    Optional<BankAccount> findByIdAndUserEmail(Long accountId, String email);

    /*
     * Admin dashboard madhe status wise accounts count/fetch karayla useful.
     */
    List<BankAccount> findByStatus(AccountStatus status);

    long countByStatus(AccountStatus status);
}