package com.fintech.Dao;

import com.fintech.Model.Entity.Invoice;
import com.fintech.Model.Enum.InvoiceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

public class InvoiceDao {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceDao.class);

    public InvoiceDao() {

    }

    public Long createInvoice(Connection con,
                              Long userId,
                              BigDecimal amount,
                              String description,
                              String paymentToken,
                              String qrCodePath,
                              LocalDateTime expiryTime) {

        String sql = """
            INSERT INTO invoices
            (user_id, amount, description,
             payment_token, qr_code_path,
             status, expiry_time)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, userId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, description);
            ps.setString(4, paymentToken);
            ps.setString(5, qrCodePath);
            ps.setString(6, InvoiceStatus.UNPAID.name());
            ps.setTimestamp(7, Timestamp.valueOf(expiryTime));

            int rows=ps.executeUpdate();

            if (rows == 0) {
                logger.error("Problem occurred while creating invoice for user id: {}", userId);
                throw new SQLException("Invoice insert failed.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id=rs.getLong(1);
                    logger.info("Invoice created with ID: {}", id);
                    return id;
                }
            }
            logger.error("Failed to create invoice for user id: {}", userId);
            throw new RuntimeException("Failed to create invoice");

        } catch (SQLException e) {

            throw new RuntimeException(e);
        }
    }

    public void updateStatus(Connection con, Long invoiceId,InvoiceStatus status) {

        String sql = """
        UPDATE invoices
        SET status = ?
        WHERE id = ?
    """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setLong(2, invoiceId);

            ps.executeUpdate();
            logger.info("Invoice status updated for ID: {}",invoiceId);
        } catch (SQLException e) {
            logger.error("Problem occurred while updating status for ID: {}",invoiceId);
            throw new RuntimeException(e);
        }
    }

    public Invoice getByTokenForUpdate(Connection con, String token) {

        String sql = """
        SELECT * FROM invoices
        WHERE payment_token = ?
        FOR UPDATE
    """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                logger.info("Invoice retrieved for token: {}",token);
                return mapInvoice(rs);
            }

            return null;

        } catch (SQLException e) {
            logger.error("Problem occurred while retrieving invoice for token: {}",token);
            throw new RuntimeException(e);
        }
    }

    public Invoice findById(Connection con, Long id) {

        String sql = """
            SELECT * FROM invoices
            WHERE id = ?
        """;

        try (
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                logger.info("Invoice retrieved for Id: {}",id);
                return mapInvoice(rs);
            }

            return null;

        } catch (SQLException e) {
            logger.error("Problem occurred while retrieving invoice for ID: {}",id);
            throw new RuntimeException(e);
        }
    }

    private Invoice mapInvoice(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();

        invoice.setId(rs.getLong("id"));
        invoice.setUserId(rs.getLong("user_id"));
        invoice.setAmount(rs.getBigDecimal("amount"));
        invoice.setDescription(rs.getString("description"));
        invoice.setPaymentToken(rs.getString("payment_token"));
        invoice.setQrCodePath(rs.getString("qr_code_path"));
        invoice.setStatus(InvoiceStatus.valueOf(rs.getString("status")));
        invoice.setExpiryTime(rs.getTimestamp("expiry_time").toLocalDateTime());
        invoice.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        return invoice;
    }
}