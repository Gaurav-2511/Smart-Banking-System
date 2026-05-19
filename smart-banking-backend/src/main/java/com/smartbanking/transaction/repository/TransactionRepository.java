package com.smartbanking.transaction.repository;

import com.smartbanking.transaction.entity.Transaction;
import com.smartbanking.transaction.entity.TransactionStatus;
import com.smartbanking.transaction.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    boolean existsByReferenceNumber(String referenceNumber);

    List<Transaction> findByBankAccountIdOrderByCreatedAtDesc(Long bankAccountId);

    List<Transaction> findByBankAccountIdAndTransactionTypeOrderByCreatedAtDesc(
            Long bankAccountId,
            TransactionType transactionType
    );

    List<Transaction> findByBankAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long bankAccountId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /*
     * Admin module:
     * All transactions latest first.
     */
    List<Transaction> findAllByOrderByCreatedAtDesc();

    /*
     * Admin dashboard:
     * Total deposited / withdrawn amount calculate karayla.
     */
    @Query("""
            SELECT SUM(t.amount)
            FROM Transaction t
            WHERE t.transactionType = :transactionType
            AND t.status = :status
            """)
    BigDecimal getTotalAmountByTypeAndStatus(
            @Param("transactionType") TransactionType transactionType,
            @Param("status") TransactionStatus status
    );

    @Query(
            value = """
                    SELECT t FROM Transaction t
                    JOIN t.bankAccount ba
                    JOIN ba.user u
                    WHERE u.email = :userEmail
                    AND (:transactionType IS NULL OR t.transactionType = :transactionType)
                    AND (:startDateTime IS NULL OR t.createdAt >= :startDateTime)
                    AND (:endDateTime IS NULL OR t.createdAt <= :endDateTime)
                    ORDER BY t.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(t) FROM Transaction t
                    JOIN t.bankAccount ba
                    JOIN ba.user u
                    WHERE u.email = :userEmail
                    AND (:transactionType IS NULL OR t.transactionType = :transactionType)
                    AND (:startDateTime IS NULL OR t.createdAt >= :startDateTime)
                    AND (:endDateTime IS NULL OR t.createdAt <= :endDateTime)
                    """
    )
    Page<Transaction> searchUserTransactions(
            @Param("userEmail") String userEmail,
            @Param("transactionType") TransactionType transactionType,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT t FROM Transaction t
                    JOIN t.bankAccount ba
                    JOIN ba.user u
                    WHERE ba.id = :accountId
                    AND u.email = :userEmail
                    AND (:transactionType IS NULL OR t.transactionType = :transactionType)
                    AND (:startDateTime IS NULL OR t.createdAt >= :startDateTime)
                    AND (:endDateTime IS NULL OR t.createdAt <= :endDateTime)
                    ORDER BY t.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(t) FROM Transaction t
                    JOIN t.bankAccount ba
                    JOIN ba.user u
                    WHERE ba.id = :accountId
                    AND u.email = :userEmail
                    AND (:transactionType IS NULL OR t.transactionType = :transactionType)
                    AND (:startDateTime IS NULL OR t.createdAt >= :startDateTime)
                    AND (:endDateTime IS NULL OR t.createdAt <= :endDateTime)
                    """
    )
    Page<Transaction> searchAccountTransactions(
            @Param("accountId") Long accountId,
            @Param("userEmail") String userEmail,
            @Param("transactionType") TransactionType transactionType,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable
    );

    @Query("""
            SELECT t FROM Transaction t
            JOIN t.bankAccount ba
            JOIN ba.user u
            WHERE ba.id = :accountId
            AND u.email = :userEmail
            ORDER BY t.createdAt DESC
            """)
    List<Transaction> findMiniStatementTransactions(
            @Param("accountId") Long accountId,
            @Param("userEmail") String userEmail,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(t) FROM Transaction t
            JOIN t.bankAccount ba
            JOIN ba.user u
            WHERE ba.id = :accountId
            AND u.email = :userEmail
            """)
    long countAccountTransactions(
            @Param("accountId") Long accountId,
            @Param("userEmail") String userEmail
    );
}