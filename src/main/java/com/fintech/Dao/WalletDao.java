package com.fintech.Dao;

import com.fintech.Model.Entity.Wallet;
import com.fintech.Model.Enum.WalletStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;

public class WalletDao {

    private static final Logger logger = LoggerFactory.getLogger(WalletDao.class);
    private final DataSource dataSource;

    public WalletDao(DataSource dataSource ) {
        this.dataSource=dataSource;
    }

    public void createWallet(Connection con, Wallet wallet) throws SQLException {

        String sql = "INSERT INTO wallets(user_id, balance, status) VALUES (?, ?, ?)";

        try (
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, wallet.getUserId());
            ps.setBigDecimal(2, wallet.getBalance());
            ps.setString(3, wallet.getStatus().name());

            int affected = ps.executeUpdate();

            if (affected == 0) {
                logger.error("Problem occurred while creating wallet for user id: {}", wallet.getUserId());
                throw new SQLException("Wallet insert failed.");
            }

            logger.info("Wallet created for user ID: {}", wallet.getUserId());
        }
    }

    public Wallet findByUserId(Connection con, Long userId) {

        String sql = "SELECT * FROM wallets WHERE user_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    logger.info("Wallet found with userId: {}", userId);
                    return mapRow(rs);
                }

                return null;
            }

        } catch (SQLException e) {
            logger.error("Wallet not found with userId: {}", userId);
            throw new RuntimeException(e);
        }
    }

    public Wallet findByUserIdForUpdate(Connection con, Long userId) {

        String sql = "SELECT * FROM wallets WHERE user_id = ? FOR UPDATE";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.info("Wallet found with userId for update: {}", userId);
                    return mapRow(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            logger.error("Wallet not found with userId for update: {}", userId);
            throw new RuntimeException(e);
        }
    }

    public void updateBalance(Connection con, Long walletId, BigDecimal newBalance) {

        String sql = "UPDATE wallets SET balance = ?, updated_at = NOW() WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, newBalance);
            ps.setLong(2, walletId);

            ps.executeUpdate();
            logger.info("Wallet balance updated with ID: {}", walletId);

        } catch (SQLException e) {
            logger.error("Wallet balance not updated with ID: {}", walletId);
            throw new RuntimeException(e);
        }
    }

    public Wallet findByWalletId(Connection connection, Long walletId) {

        String sql = "SELECT * FROM wallets WHERE id = ? FOR UPDATE";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, walletId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.info("Wallet found with ID for update: {}", walletId);
                    return mapRow(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            logger.error("Wallet not found with ID for update: {}", walletId);
            throw new RuntimeException(e);
        }
    }

    private Wallet mapRow(ResultSet rs) throws SQLException {

        Wallet wallet = new Wallet();

        wallet.setId(rs.getLong("id"));
        wallet.setUserId(rs.getLong("user_id"));
        wallet.setBalance(rs.getBigDecimal("balance"));
        wallet.setStatus(
                WalletStatus.valueOf(rs.getString("status"))
        );
        wallet.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        wallet.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        return wallet;
    }


}
