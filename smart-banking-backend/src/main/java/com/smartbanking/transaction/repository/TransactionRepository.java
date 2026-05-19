package com.smartbanking.transaction.repository;

import com.smartbanking.transaction.entity.Transaction;
import com.smartbanking.transaction.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	/*
	 * Reference number varun transaction find karaycha.
	 */
	Optional<Transaction> findByReferenceNumber(String referenceNumber);

	/*
	 * Reference number duplicate aahe ka check karaycha.
	 */
	boolean existsByReferenceNumber(String referenceNumber);

	/*
	 * Specific account che transactions latest first. Old method Phase 6 madhun
	 * ठेवली आहे.
	 */
	List<Transaction> findByBankAccountIdOrderByCreatedAtDesc(Long bankAccountId);

	/*
	 * Account + transaction type filter. Old method Phase 6 madhun ठेवली आहे.
	 */
	List<Transaction> findByBankAccountIdAndTransactionTypeOrderByCreatedAtDesc(Long bankAccountId,
			TransactionType transactionType);

	/*
	 * Account + date range filter. Old method Phase 6 madhun ठेवली आहे.
	 */
	List<Transaction> findByBankAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long bankAccountId,
			LocalDateTime startDate, LocalDateTime endDate);

	/*
	 * Logged-in user che all transactions with optional filters.
	 */
	@Query(value = """
			SELECT t FROM Transaction t
			JOIN t.bankAccount ba
			JOIN ba.user u
			WHERE u.email = :userEmail
			AND (:transactionType IS NULL OR t.transactionType = :transactionType)
			AND (:startDateTime IS NULL OR t.createdAt >= :startDateTime)
			AND (:endDateTime IS NULL OR t.createdAt <= :endDateTime)
			ORDER BY t.createdAt DESC
			""", countQuery = """
			SELECT COUNT(t) FROM Transaction t
			JOIN t.bankAccount ba
			JOIN ba.user u
			WHERE u.email = :userEmail
			AND (:transactionType IS NULL OR t.transactionType = :transactionType)
			AND (:startDateTime IS NULL OR t.createdAt >= :startDateTime)
			AND (:endDateTime IS NULL OR t.createdAt <= :endDateTime)
			""")
	Page<Transaction> searchUserTransactions(@Param("userEmail") String userEmail,
			@Param("transactionType") TransactionType transactionType,
			@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime,
			Pageable pageable);

	/*
	 * Specific account transactions with optional filters. User ownership check
	 * query madhyeच आहे.
	 */
	@Query(value = """
			SELECT t FROM Transaction t
			JOIN t.bankAccount ba
			JOIN ba.user u
			WHERE ba.id = :accountId
			AND u.email = :userEmail
			AND (:transactionType IS NULL OR t.transactionType = :transactionType)
			AND (:startDateTime IS NULL OR t.createdAt >= :startDateTime)
			AND (:endDateTime IS NULL OR t.createdAt <= :endDateTime)
			ORDER BY t.createdAt DESC
			""", countQuery = """
			SELECT COUNT(t) FROM Transaction t
			JOIN t.bankAccount ba
			JOIN ba.user u
			WHERE ba.id = :accountId
			AND u.email = :userEmail
			AND (:transactionType IS NULL OR t.transactionType = :transactionType)
			AND (:startDateTime IS NULL OR t.createdAt >= :startDateTime)
			AND (:endDateTime IS NULL OR t.createdAt <= :endDateTime)
			""")
	Page<Transaction> searchAccountTransactions(@Param("accountId") Long accountId,
			@Param("userEmail") String userEmail, @Param("transactionType") TransactionType transactionType,
			@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime,
			Pageable pageable);

	/*
	 * Mini statement sathi latest transactions.
	 */
	@Query("""
			SELECT t FROM Transaction t
			JOIN t.bankAccount ba
			JOIN ba.user u
			WHERE ba.id = :accountId
			AND u.email = :userEmail
			ORDER BY t.createdAt DESC
			""")
	List<Transaction> findMiniStatementTransactions(@Param("accountId") Long accountId,
			@Param("userEmail") String userEmail, Pageable pageable);

	/*
	 * Mini statement मध्ये total transaction count दाखवण्यासाठी.
	 */
	@Query("""
			SELECT COUNT(t) FROM Transaction t
			JOIN t.bankAccount ba
			JOIN ba.user u
			WHERE ba.id = :accountId
			AND u.email = :userEmail
			""")
	long countAccountTransactions(@Param("accountId") Long accountId, @Param("userEmail") String userEmail);
}