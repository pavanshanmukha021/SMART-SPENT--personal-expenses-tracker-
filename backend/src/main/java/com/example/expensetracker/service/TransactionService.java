package com.example.expensetracker.service;

import com.example.expensetracker.dto.SummaryDTO;
import com.example.expensetracker.entity.Transaction;
import com.example.expensetracker.entity.TransactionType;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.repository.TransactionRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = Objects.requireNonNull(transactionRepository, "TransactionRepository cannot be null");
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }

    public List<Transaction> getAllTransactions() {
        User currentUser = getCurrentUser();
        return transactionRepository.findByUserOrderByDateDesc(currentUser);
    }

    @Transactional
    public Transaction addTransaction(Transaction transaction) {
        Objects.requireNonNull(transaction, "Transaction cannot be null");
        User currentUser = getCurrentUser();
        transaction.setUser(currentUser);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Objects.requireNonNull(id, "Transaction ID cannot be null");
        User currentUser = getCurrentUser();
        Transaction transaction = transactionRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Transaction with ID " + id + " not found or access denied"));
        transactionRepository.delete(transaction);
    }

    public SummaryDTO getSummary() {
        User currentUser = getCurrentUser();
        BigDecimal totalIncome = transactionRepository.sumByTypeAndUser(TransactionType.INCOME, currentUser);
        BigDecimal totalExpense = transactionRepository.sumByTypeAndUser(TransactionType.EXPENSE, currentUser);
        BigDecimal balance = (totalIncome != null ? totalIncome : BigDecimal.ZERO)
                .subtract(totalExpense != null ? totalExpense : BigDecimal.ZERO);

        return new SummaryDTO(
            totalIncome != null ? totalIncome : BigDecimal.ZERO,
            totalExpense != null ? totalExpense : BigDecimal.ZERO,
            balance
        );
    }

    public SummaryDTO getSummaryByPeriod(LocalDate startDate, LocalDate endDate) {
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        User currentUser = getCurrentUser();
        BigDecimal totalIncome = transactionRepository.sumByTypeAndUserAndDateBetween(
            TransactionType.INCOME, currentUser, startDateTime, endDateTime);
        BigDecimal totalExpense = transactionRepository.sumByTypeAndUserAndDateBetween(
            TransactionType.EXPENSE, currentUser, startDateTime, endDateTime);
            
        BigDecimal balance = (totalIncome != null ? totalIncome : BigDecimal.ZERO)
                .subtract(totalExpense != null ? totalExpense : BigDecimal.ZERO);

        return new SummaryDTO(
            totalIncome != null ? totalIncome : BigDecimal.ZERO,
            totalExpense != null ? totalExpense : BigDecimal.ZERO,
            balance
        );
    }

    // --- NEW METHOD 2 ---
    public List<Transaction> getTransactionsByPeriod(LocalDate startDate, LocalDate endDate) {
        User currentUser = getCurrentUser();
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return transactionRepository.findByUserAndDateBetween(currentUser, startDateTime, endDateTime);
    }
}