package com.smartbanking.transaction.service.impl;

import com.smartbanking.account.entity.AccountStatus;
import com.smartbanking.account.entity.BankAccount;
import com.smartbanking.account.repository.BankAccountRepository;
import com.smartbanking.exception.BadRequestException;
import com.smartbanking.exception.ResourceNotFoundException;
import com.smartbanking.transaction.dto.DepositRequest;
import com.smartbanking.transaction.dto.TransactionResponse;
import com.smartbanking.transaction.dto.TransferRequest;
import com.smartbanking.transaction.dto.WithdrawRequest;
import com.smartbanking.transaction.entity.Transaction;
import com.smartbanking.transaction.entity.TransactionStatus;
import com.smartbanking.transaction.entity.TransactionType;
import com.smartbanking.transaction.repository.TransactionRepository;
import com.smartbanking.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    
    /*
     * Deposit method does two tasks:
     * 1. Updates the account balance
     * 2. Saves the transaction record
     *
     * So we use @Transactional here.
     */
    @Override
    @Transactional
    public TransactionResponse deposit(DepositRequest request, String userEmail) {

        validateAmount(request.getAmount());

        BankAccount account = bankAccountRepository.findByIdAndUserEmail(request.getAccountId(), userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found or you do not have access to this account"
                ));

        validateAccountActive(account);

        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);

        BankAccount savedAccount = bankAccountRepository.save(account);

        Transaction transaction = createTransaction(
                savedAccount,
                TransactionType.DEPOSIT,
                request.getAmount(),
                savedAccount.getAccountNumber(),
                savedAccount.getAccountNumber(),
                getDescriptionOrDefault(request.getDescription(), "Money deposited successfully")
        );

        Transaction savedTransaction = transactionRepository.save(transaction);

        return mapToTransactionResponse(savedTransaction, savedAccount.getBalance());
    }

    /*
     * Withdraw मध्ये balance कमी करतो आणि transaction record save करतो.
     */
    @Override
    @Transactional
    public TransactionResponse withdraw(WithdrawRequest request, String userEmail) {

        validateAmount(request.getAmount());

        BankAccount account = bankAccountRepository.findByIdAndUserEmail(request.getAccountId(), userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found or you do not have access to this account"
                ));

        validateAccountActive(account);
        validateSufficientBalance(account, request.getAmount());

        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);

        BankAccount savedAccount = bankAccountRepository.save(account);

        Transaction transaction = createTransaction(
                savedAccount,
                TransactionType.WITHDRAW,
                request.getAmount(),
                savedAccount.getAccountNumber(),
                savedAccount.getAccountNumber(),
                getDescriptionOrDefault(request.getDescription(), "Money withdrawn successfully")
        );

        Transaction savedTransaction = transactionRepository.save(transaction);

        return mapToTransactionResponse(savedTransaction, savedAccount.getBalance());
    }

    /*
     * Fund transfer सर्वात important operation आहे.
     * Sender कडून पैसे cut होतात आणि receiver कडे add होतात.
     * मध्ये error आली तर complete rollback होण्यासाठी @Transactional वापरले आहे.
     */
    @Override
    @Transactional
    public TransactionResponse transfer(TransferRequest request, String userEmail) {

        validateAmount(request.getAmount());

        BankAccount senderAccount = bankAccountRepository.findByIdAndUserEmail(
                        request.getFromAccountId(),
                        userEmail
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sender account not found or you do not have access to this account"
                ));

        BankAccount receiverAccount = bankAccountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver account not found"));

        if (senderAccount.getAccountNumber().equals(receiverAccount.getAccountNumber())) {
            throw new BadRequestException("Sender and receiver account cannot be same");
        }

        validateAccountActive(senderAccount);
        validateAccountActive(receiverAccount);
        validateSufficientBalance(senderAccount, request.getAmount());

        senderAccount.setBalance(senderAccount.getBalance().subtract(request.getAmount()));
        receiverAccount.setBalance(receiverAccount.getBalance().add(request.getAmount()));

        BankAccount savedSenderAccount = bankAccountRepository.save(senderAccount);
        BankAccount savedReceiverAccount = bankAccountRepository.save(receiverAccount);

        /*
         * Sender side transaction record.
         */
        Transaction senderTransaction = createTransaction(
                savedSenderAccount,
                TransactionType.TRANSFER,
                request.getAmount(),
                savedSenderAccount.getAccountNumber(),
                savedReceiverAccount.getAccountNumber(),
                getDescriptionOrDefault(
                        request.getDescription(),
                        "Money transferred to account " + savedReceiverAccount.getAccountNumber()
                )
        );

        Transaction savedSenderTransaction = transactionRepository.save(senderTransaction);

        /*
         * Receiver side transaction record.
         * Receiver ला पण history मध्ये credit entry दिसावी म्हणून हा record save करतो.
         */
        Transaction receiverTransaction = createTransaction(
                savedReceiverAccount,
                TransactionType.TRANSFER,
                request.getAmount(),
                savedSenderAccount.getAccountNumber(),
                savedReceiverAccount.getAccountNumber(),
                "Money received from account " + savedSenderAccount.getAccountNumber()
        );

        transactionRepository.save(receiverTransaction);

        return mapToTransactionResponse(savedSenderTransaction, savedSenderAccount.getBalance());
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than 0");
        }
    }

    private void validateAccountActive(BankAccount account) {

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is not active");
        }
    }

    private void validateSufficientBalance(BankAccount account, BigDecimal amount) {

        if (account.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient balance");
        }
    }

    private Transaction createTransaction(
            BankAccount account,
            TransactionType transactionType,
            BigDecimal amount,
            String fromAccountNumber,
            String toAccountNumber,
            String description
    ) {

        return Transaction.builder()
                .referenceNumber(generateUniqueReferenceNumber())
                .transactionType(transactionType)
                .status(TransactionStatus.SUCCESS)
                .amount(amount)
                .fromAccountNumber(fromAccountNumber)
                .toAccountNumber(toAccountNumber)
                .description(description)
                .bankAccount(account)
                .build();
    }

    private String generateUniqueReferenceNumber() {

        String referenceNumber;

        do {
            String dateTimePart = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            String randomPart = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();

            referenceNumber = "TXN" + dateTimePart + randomPart;

        } while (transactionRepository.existsByReferenceNumber(referenceNumber));

        return referenceNumber;
    }

    private String getDescriptionOrDefault(String description, String defaultDescription) {

        if (description == null || description.trim().isEmpty()) {
            return defaultDescription;
        }

        return description.trim();
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction, BigDecimal currentBalance) {

        BankAccount account = transaction.getBankAccount();

        return TransactionResponse.builder()
                .transactionId(transaction.getId())
                .referenceNumber(transaction.getReferenceNumber())
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .amount(transaction.getAmount())
                .currentBalance(currentBalance)
                .fromAccountNumber(transaction.getFromAccountNumber())
                .toAccountNumber(transaction.getToAccountNumber())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}