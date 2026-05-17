package com.smartbanking.transaction.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartbanking.transaction.entity.Transaction;
import com.smartbanking.transaction.entity.TransactionType;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	Optional<Transaction> findByReferenceNumber(String referenceNumber);

	
	boolean existsByReferenceNumber(String referenceNumber);

	
	List<Transaction> findByBankAccountIdOrderByCreatedAtDesc(Long bankAccountId);

	
	// Account + transaction type filter.
	List<Transaction> findByBankAccountIdAndTransactionTypeOrderByCreatedAtDesc(Long bankAccountId,
			TransactionType transactionType);

	
	// Account + date range filter.
	List<Transaction> findByBankAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(
			Long bankAccountId,
			LocalDateTime startDate,
			LocalDateTime endDate);
}
