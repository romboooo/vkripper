package org.example.finance.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class UserBalanceRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserBalanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean debitIfSufficientBalance(Long userId, BigDecimal amount) {
        int updatedRows = jdbcTemplate.update(
                "update users set balance = balance - ? where id = ? and balance >= ?",
                amount,
                userId,
                amount
        );

        return updatedRows == 1;
    }

    public boolean credit(Long userId, BigDecimal amount) {
        int updatedRows = jdbcTemplate.update(
                "update users set balance = balance + ? where id = ?",
                amount,
                userId
        );

        return updatedRows == 1;
    }
}
