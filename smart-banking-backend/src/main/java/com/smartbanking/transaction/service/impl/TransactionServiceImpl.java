package com.smartbanking.transaction.service.impl;

import com.smartbanking.account.entity.AccountStatus;
import com.smartbanking.account.entity.BankAccount;
import com.smartbanking.account.repository.BankAccountRepository;
import com.smartbanking.exception.BadRequestException;
import com.smartbanking.exception.ResourceNotFoundException;
import com.smartbanking.transaction.dto.DepositRequest;
import com.smartbanking.transaction.dto.MiniStatementResponse;
import com.smartbanking.transaction.dto.TransactionResponse;
import com.smartbanking.transaction.dto.TransferRequest;
import com.smartbanking.transaction.dto.WithdrawRequest;
import com.smartbanking.transaction.entity.Transaction;
import com.smartbanking.transaction.entity.TransactionStatus;
import com.smartbanking.transaction.entity.TransactionType;
import com.smartbanking.transaction.repository.TransactionRepository;
import com.smartbanking.transaction.service.TransactionService;
import com.smartbanking.util.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

	private final BankAccountRepository bankAccountRepository;
	private final TransactionRepository transactionRepository;

	@Override
	@Transactional
	public TransactionResponse deposit(DepositRequest request, String userEmail) {

		validateAmount(request.getAmount());

		BankAccount account = bankAccountRepository.findByIdAndUserEmail(request.getAccountId(), userEmail).orElseThrow(
				() -> new ResourceNotFoundException("Account not found or you do not have access to this account"));

		validateAccountActive(account);

		BigDecimal newBalance = account.getBalance().add(request.getAmount());
		account.setBalance(newBalance);

		BankAccount savedAccount = bankAccountRepository.save(account);

		Transaction transaction = createTransaction(savedAccount, TransactionType.DEPOSIT, request.getAmount(),
				savedAccount.getAccountNumber(), savedAccount.getAccountNumber(),
				getDescriptionOrDefault(request.getDescription(), "Money deposited successfully"));

		Transaction savedTransaction = transactionRepository.save(transaction);

		return mapToTransactionResponse(savedTransaction, savedAccount.getBalance());
	}

	@Override
	@Transactional
	public TransactionResponse withdraw(WithdrawRequest request, String userEmail) {

		validateAmount(request.getAmount());

		BankAccount account = bankAccountRepository.findByIdAndUserEmail(request.getAccountId(), userEmail).orElseThrow(
				() -> new ResourceNotFoundException("Account not found or you do not have access to this account"));

		validateAccountActive(account);
		validateSufficientBalance(account, request.getAmount());

		BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
		account.setBalance(newBalance);

		BankAccount savedAccount = bankAccountRepository.save(account);

		Transaction transaction = createTransaction(savedAccount, TransactionType.WITHDRAW, request.getAmount(),
				savedAccount.getAccountNumber(), savedAccount.getAccountNumber(),
				getDescriptionOrDefault(request.getDescription(), "Money withdrawn successfully"));

		Transaction savedTransaction = transactionRepository.save(transaction);

		return mapToTransactionResponse(savedTransaction, savedAccount.getBalance());
	}

	@Override
	@Transactional
	public TransactionResponse transfer(TransferRequest request, String userEmail) {

		validateAmount(request.getAmount());

		BankAccount senderAccount = bankAccountRepository.findByIdAndUserEmail(request.getFromAccountId(), userEmail)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Sender account not found or you do not have access to this account"));

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

		Transaction senderTransaction = createTransaction(savedSenderAccount, TransactionType.TRANSFER,
				request.getAmount(), savedSenderAccount.getAccountNumber(), savedReceiverAccount.getAccountNumber(),
				getDescriptionOrDefault(request.getDescription(),
						"Money transferred to account " + savedReceiverAccount.getAccountNumber()));

		Transaction savedSenderTransaction = transactionRepository.save(senderTransaction);

		Transaction receiverTransaction = createTransaction(savedReceiverAccount, TransactionType.TRANSFER,
				request.getAmount(), savedSenderAccount.getAccountNumber(), savedReceiverAccount.getAccountNumber(),
				"Money received from account " + savedSenderAccount.getAccountNumber());

		transactionRepository.save(receiverTransaction);

		return mapToTransactionResponse(savedSenderTransaction, savedSenderAccount.getBalance());
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<TransactionResponse> getMyTransactions(String userEmail, TransactionType transactionType,
			LocalDate startDate, LocalDate endDate, int page, int size) {

		validatePagination(page, size);
		validateDateRange(startDate, endDate);

		Pageable pageable = PageRequest.of(page, size);

		Page<Transaction> transactionPage = transactionRepository.searchUserTransactions(userEmail, transactionType,
				toStartDateTime(startDate), toEndDateTime(endDate), pageable);

		Page<TransactionResponse> responsePage = transactionPage
				.map(transaction -> mapToTransactionResponse(transaction, transaction.getBankAccount().getBalance()));

		return PageResponse.fromPage(responsePage);
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<TransactionResponse> getAccountTransactions(Long accountId, String userEmail,
			TransactionType transactionType, LocalDate startDate, LocalDate endDate, int page, int size) {

		validatePagination(page, size);
		validateDateRange(startDate, endDate);

		/*
		 * First ownership check. यामुळे user दुसऱ्याचा account history पाहू शकणार नाही.
		 */
		bankAccountRepository.findByIdAndUserEmail(accountId, userEmail).orElseThrow(
				() -> new ResourceNotFoundException("Account not found or you do not have access to this account"));

		Pageable pageable = PageRequest.of(page, size);

		Page<Transaction> transactionPage = transactionRepository.searchAccountTransactions(accountId, userEmail,
				transactionType, toStartDateTime(startDate), toEndDateTime(endDate), pageable);

		Page<TransactionResponse> responsePage = transactionPage
				.map(transaction -> mapToTransactionResponse(transaction, transaction.getBankAccount().getBalance()));

		return PageResponse.fromPage(responsePage);
	}

	@Override
	@Transactional(readOnly = true)
	public MiniStatementResponse getMiniStatement(Long accountId, String userEmail) {

		BankAccount account = bankAccountRepository.findByIdAndUserEmail(accountId, userEmail).orElseThrow(
				() -> new ResourceNotFoundException("Account not found or you do not have access to this account"));

		/*
		 * Mini statement मध्ये latest 10 transactions दाखवतो.
		 */
		Pageable latestTen = PageRequest.of(0, 10);

		List<Transaction> transactions = transactionRepository.findMiniStatementTransactions(accountId, userEmail,
				latestTen);

		List<TransactionResponse> transactionResponses = transactions.stream()
				.map(transaction -> mapToTransactionResponse(transaction, account.getBalance())).toList();

		long totalTransactions = transactionRepository.countAccountTransactions(accountId, userEmail);

		return MiniStatementResponse.builder().bankName("Smart Banking System").userName(account.getUser().getName())
				.userEmail(account.getUser().getEmail()).accountId(account.getId())
				.accountNumber(account.getAccountNumber()).accountType(account.getAccountType())
				.status(account.getStatus()).currentBalance(account.getBalance()).generatedAt(LocalDateTime.now())
				.totalTransactions(totalTransactions).transactions(transactionResponses).build();
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

	private void validatePagination(int page, int size) {

		if (page < 0) {
			throw new BadRequestException("Page number cannot be negative");
		}

		if (size <= 0) {
			throw new BadRequestException("Page size must be greater than 0");
		}

		if (size > 50) {
			throw new BadRequestException("Page size cannot be greater than 50");
		}
	}

	private void validateDateRange(LocalDate startDate, LocalDate endDate) {

		if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
			throw new BadRequestException("Start date cannot be after end date");
		}
	}

	private LocalDateTime toStartDateTime(LocalDate startDate) {

		if (startDate == null) {
			return null;
		}

		return startDate.atStartOfDay();
	}

	private LocalDateTime toEndDateTime(LocalDate endDate) {

		if (endDate == null) {
			return null;
		}

		return endDate.atTime(LocalTime.MAX);
	}

	private Transaction createTransaction(BankAccount account, TransactionType transactionType, BigDecimal amount,
			String fromAccountNumber, String toAccountNumber, String description) {

		return Transaction.builder().referenceNumber(generateUniqueReferenceNumber()).transactionType(transactionType)
				.status(TransactionStatus.SUCCESS).amount(amount).fromAccountNumber(fromAccountNumber)
				.toAccountNumber(toAccountNumber).description(description).bankAccount(account).build();
	}

	private String generateUniqueReferenceNumber() {

		String referenceNumber;

		do {
			String dateTimePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

			String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

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

		return TransactionResponse.builder().transactionId(transaction.getId())
				.referenceNumber(transaction.getReferenceNumber()).accountId(account.getId())
				.accountNumber(account.getAccountNumber()).transactionType(transaction.getTransactionType())
				.status(transaction.getStatus()).amount(transaction.getAmount()).currentBalance(currentBalance)
				.fromAccountNumber(transaction.getFromAccountNumber()).toAccountNumber(transaction.getToAccountNumber())
				.description(transaction.getDescription()).createdAt(transaction.getCreatedAt()).build();
	}
}