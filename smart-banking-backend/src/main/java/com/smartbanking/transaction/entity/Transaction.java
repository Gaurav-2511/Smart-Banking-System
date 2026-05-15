package com.smartbanking.transaction.entity;

import com.smartbanking.account.entity.BankAccount;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Unique transaction reference number.
     * Later transaction module madhe auto-generate karu.
     */
    @Column(name = "reference_number", nullable = false, unique = true, length = 50)
    private String referenceNumber;

    /*
     * DEPOSIT, WITHDRAW, TRANSFER
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType transactionType;

    /*
     * SUCCESS, FAILED, PENDING
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    /*
     * Transaction amount.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /*
     * For deposit/withdraw this can be same account.
     * For transfer, this can store sender account number.
     */
    @Column(name = "from_account_number", length = 30)
    private String fromAccountNumber;

    /*
     * For transfer, receiver account number.
     */
    @Column(name = "to_account_number", length = 30)
    private String toAccountNumber;

    /*
     * Extra message/remark.
     */
    @Column(length = 255)
    private String description;

    /*
     * Many transactions belong to one bank account.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = TransactionStatus.SUCCESS;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}