package com.fintech.Service;

import com.fintech.Dao.UserDao;
import com.fintech.Dao.WalletDao;
import com.fintech.Exception.ConflictException;
import com.fintech.Exception.InternalServerException;
import com.fintech.Exception.UnauthorizedException;
import com.fintech.Model.Entity.User;
import com.fintech.Model.Entity.Wallet;
import com.fintech.Model.Enum.UserRole;
import com.fintech.Model.Enum.WalletStatus;
import com.fintech.Util.JwtUtil;
import com.fintech.Util.PasswordUtil;
import com.fintech.Util.UserAccountValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final DataSource dataSource;
    private final UserDao userDao;
    private final WalletDao walletDao;
    public AuthService(DataSource dataSource) {
        this.dataSource = dataSource;
        this.userDao = new UserDao(dataSource);
        this.walletDao = new WalletDao(dataSource);
    }

    public void register(String fullName, String email, String rawPassword) {

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try {

                if (userDao.existsByEmail(connection, email)) {
                    logger.warn("Email already registered with another user account for {}",email);

                    throw new ConflictException("Email already exists");
                }

                User user = new User();

                UserAccountValidator.validateName(fullName);
                user.setFullName(fullName);

                UserAccountValidator.validateEmail(email);
                user.setEmail(email);

                UserAccountValidator.validatePassword(rawPassword);
                String hashed = PasswordUtil.hash(rawPassword);
                user.setPasswordHash(hashed);

                user.setRole(UserRole.USER);

                Long userId = userDao.save(connection, user);

                Wallet wallet = new Wallet();
                wallet.setUserId(userId);
                wallet.setBalance(BigDecimal.ZERO);
                wallet.setStatus(WalletStatus.ACTIVE);

                walletDao.createWallet(connection,wallet);

                connection.commit();

                logger.info("User registration successful for email: {}", email);

            }
            catch (ConflictException e) {
                connection.rollback();
                throw e;
            }
            catch (Exception e) {
                connection.rollback();
                logger.error("Registration failed. Email: {}", email, e);
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Connection error during registration. Email: {}", email, e);
            throw new InternalServerException("Database error");
        }
    }

    public String login(String email, String rawPassword) {
        try(Connection connection = dataSource.getConnection()) {

            User user = userDao.findByEmail(connection,email);

            if (user == null) {
                logger.warn("Login failed. Email not found: {}", email);
                throw new UnauthorizedException("Invalid credentials");
            }

            boolean matches = PasswordUtil.verify(rawPassword, user.getPasswordHash());

            if (!matches) {
                logger.warn("Login failed. Invalid password for email: {}", email);
                throw new UnauthorizedException("Invalid credentials");
            }

            String token = JwtUtil.generateToken(user.getId(), user.getEmail());

            logger.info("Login successful for email: {}", email);

            return token;

        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Login failed due to database error. Email: {}", email, e);
            throw new InternalServerException("Database error");
        }
    }

    public User findById(Long userId){
        try{
            return userDao.findById(userId);
        }
        catch (Exception e){
            logger.error("Error occurred while retrieving user account with ID: {}",userId, e);
            throw new InternalServerException("Database error");
        }
    }
}