package com.smartbanking.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartbanking.account.entity.AccountStatus;
import com.smartbanking.account.entity.BankAccount;
import java.util.List;
import java.util.Optional;


@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long>{

	Optional<BankAccount> findByAccountNumber(String accountNumber);
	
	boolean existsByAccountNumber(String accountNumber);
	
	List<BankAccount> findByUserId(Long userId);
	
	List<BankAccount> findByStatus(AccountStatus status);

	long countByStatus(AccountStatus status);
}
