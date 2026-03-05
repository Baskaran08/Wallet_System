package com.fintech.Dao;


import com.fintech.Model.Entity.User;
import com.fintech.Model.Enum.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;

public class UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource=dataSource;
    }

    public Long save(Connection con,  User user) throws SQLException {

        String sql = "INSERT INTO users(full_name, email, password_hash, role) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole().name());

            int affected = ps.executeUpdate();

            if (affected == 0) {
                logger.error("Problem occurred while creating user for email: {}", user.getEmail());
                throw new SQLException("User insert failed.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    logger.info("User created with ID: {}", id);
                    return id;
                }
            }
        }

        throw new SQLException("User ID not generated.");
    }

    public boolean existsByEmail(Connection con, String email) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ?";

        try (
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                boolean exists=rs.next();
                logger.info("User exists with email: {} exist:{}", email,exists);
                return exists;
            }
        }
    }

    public User findByEmail(Connection con, String email) {

        String sql = "SELECT * FROM users WHERE email = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                logger.info("User found with email: {}", email);
                return mapRow(rs);
            }

            return null;

        } catch (SQLException e) {
            logger.error("User not found with email: {}", email);
            throw new RuntimeException(e);
        }
    }

    public User findById(Long id) {

        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection con=dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                logger.info("User found with ID: {}", id);
                return mapRow(rs);
            }

            return null;

        } catch (SQLException e) {
            logger.error("User not found with ID: {}", id);
            throw new RuntimeException(e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        return user;
    }
}
