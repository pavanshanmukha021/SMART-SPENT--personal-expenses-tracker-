package com.example.expensetracker.repository;

import com.example.expensetracker.entity.Transaction;
import com.example.expensetracker.entity.TransactionType;
import com.example.expensetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Find all transactions for a specific user
    List<Transaction> findByUserOrderByDateDesc(User user);
    
    // Find a transaction by id and user (for security)
    Optional<Transaction> findByIdAndUser(Long id, User user);
    
    // Sum by type for a specific user
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type AND t.user = :user")
    BigDecimal sumByTypeAndUser(@Param("type") TransactionType type, @Param("user") User user);
    
    // Sum by type and date range for a specific user
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type AND t.user = :user AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumByTypeAndUserAndDateBetween(
        @Param("type") TransactionType type,
        @Param("user") User user,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Find transactions by date range for a specific user
    List<Transaction> findByUserAndDateBetween(User user, LocalDateTime startDate, LocalDateTime endDate);
}
