package com.fintech.Dao;

import com.fintech.Model.Entity.Transaction;
import com.fintech.Model.Enum.TransactionStatus;
import com.fintech.Model.Enum.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDao {

    private static final Logger logger = LoggerFactory.getLogger(TransactionDao.class);
    private final DataSource dataSource;

    public TransactionDao(DataSource dataSource) {
        this.dataSource=dataSource;
    }

    public Long createTransaction(Connection con,
                                  Long senderWalletId,
                                  Long receiverWalletId,
                                  BigDecimal amount,
                                  TransactionType type) {

        String sql = """
            INSERT INTO transactions
            (sender_wallet_id, receiver_wallet_id, amount, type, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, NOW(), NOW())
        """;

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, senderWalletId);
            ps.setLong(2, receiverWalletId);
            ps.setBigDecimal(3, amount);
            ps.setString(4, type.name());
            ps.setString(5, TransactionStatus.INITIATED.name());

            int rows = ps.executeUpdate();

            if (rows == 0) {
                logger.error("Problem occurred while creating transaction for sender wallet id: {} and receiver wallet id: {}", senderWalletId,receiverWalletId);
                throw new SQLException("transaction insert failed.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id=rs.getLong(1);
                    logger.info("Transaction created with ID: {}", id);
                    return id;
                }
            }
            logger.error("Failed to create transaction for sender wallet id: {} and receiver wallet id: {}", senderWalletId,receiverWalletId);
            throw new RuntimeException("Failed to create transaction");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Long createTransactionForDeposit(Connection con, Long walletId, BigDecimal amount, TransactionType transactionType) {
        String sql = """
            INSERT INTO transactions
            (receiver_wallet_id, amount, type, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, NOW(), NOW())
        """;

        try (
                PreparedStatement ps =
                        con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, walletId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, transactionType.name());
            ps.setString(4, TransactionStatus.INITIATED.name());

            int rows = ps.executeUpdate();

            if (rows == 0) {
                logger.error("Problem occurred while creating transaction deposit for wallet id: {}", walletId);
                throw new SQLException("transaction insert failed.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id=rs.getLong(1);
                    logger.info("Transaction created for deposit with ID: {}", id);
                    return id;
                }
            }
            throw new RuntimeException("Failed to create transaction");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateStatus(Connection con,
                             Long transactionId,
                             String status) {

        String sql = "UPDATE transactions SET status = ?, updated_at = NOW() WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setLong(2, transactionId);

            ps.executeUpdate();
            logger.info("Transaction status updated with ID: {}", transactionId);
        } catch (SQLException e) {
            logger.error("Problem occurred while updating transaction status with ID: {}", transactionId);
            throw new RuntimeException(e);
        }
    }

    public void updateInvoiceId(Connection con,
                             Long transactionId,
                             Long invoiceId) {

        String sql = "UPDATE transactions SET invoice_id = ?, updated_at = NOW() WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, invoiceId);
            ps.setLong(2, transactionId);

            ps.executeUpdate();
            logger.info("Invoice id is updated with transaction  ID: {}", transactionId);
        } catch (SQLException e) {
            logger.error("Problem occurred while updating transaction invoice id for ID: {}", transactionId);
            throw new RuntimeException(e);
        }
    }

    public List<Transaction> findByWalletId(Long walletId, int offset, int size) {
        String sql = """
            SELECT * FROM transactions
            WHERE sender_wallet_id = ?
               OR receiver_wallet_id = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
        """;

        List<Transaction> list = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, walletId);
            ps.setLong(2, walletId);
            ps.setInt(3, size);
            ps.setInt(4, offset);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                logger.info("Transactions found for wallet ID: {}", walletId);
                list.add(mapRow(rs));
            }

        } catch (Exception e) {
            logger.error("Error occurred while getting transactions for wallet ID: {}", walletId);
            throw new RuntimeException(e);
        }

        return list;
    }

    public int countByWalletId(Long walletId) {
        String sql = """
            SELECT COUNT(*) FROM transactions
            WHERE sender_wallet_id = ?
               OR receiver_wallet_id = ?
        """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, walletId);
            ps.setLong(2, walletId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                logger.info("Total transactions found for wallet ID: {}", walletId);
                return rs.getInt(1);
            }

        } catch (Exception e) {
            logger.error("Error occurred while getting total transactions for wallet ID: {}", walletId);
            throw new RuntimeException(e);
        }

        return 0;
    }

    public List<Transaction> findAll(int offset, int limit) {

        String sql = """
            SELECT * FROM transactions
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
        """;

        List<Transaction> list = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ps.setInt(2, offset);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                logger.info("Transactions of all users are retrieved");
                list.add(mapRow(rs));
            }

        } catch (Exception e) {
            logger.error("Error occurred while retrieving transactions of all users");
            throw new RuntimeException(e);
        }

        return list;
    }

    public int countAll() {
        String sql = """
            SELECT COUNT(*) FROM transactions
        """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                logger.info("Total transactions of all users are retrieved");
                return rs.getInt(1);
            }

        } catch (Exception e) {
            logger.error("Error occurred while retrieving total transactions of all users");
            throw new RuntimeException(e);
        }

        return 0;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {

        Transaction tx = new Transaction();

        tx.setId(rs.getLong("id"));
        tx.setSenderWalletId(rs.getLong("sender_wallet_id"));
        tx.setReceiverWalletId(rs.getLong("receiver_wallet_id"));
        tx.setAmount(rs.getBigDecimal("amount"));
        tx.setType(TransactionType.valueOf(rs.getString("type")));
        tx.setStatus(TransactionStatus.valueOf(rs.getString("status")));
        tx.setInvoiceId(rs.getLong("invoice_id"));
        tx.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        tx.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        return tx;
    }


}