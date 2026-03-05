package com.fintech.Service;

import com.fintech.Dao.TransactionDao;
import com.fintech.Dao.WalletDao;
import com.fintech.Exception.BadRequestException;
import com.fintech.Exception.InternalServerException;
import com.fintech.Exception.NotFoundException;
import com.fintech.Model.Entity.Wallet;
import com.fintech.Model.Enum.TransactionStatus;
import com.fintech.Model.Enum.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Map;

public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    private final DataSource dataSource;
    private final WalletDao walletDao;
    private final TransactionDao transactionDao;


    public WalletService(DataSource dataSource) {
        this.dataSource = dataSource;
        this.walletDao = new WalletDao(dataSource);
        this.transactionDao=new TransactionDao(dataSource);
    }

    public Map<String, Object> getWalletByUserId(Long userId) {

        try(Connection connection = dataSource.getConnection())  {

            Wallet wallet = walletDao.findByUserId( connection, userId);

            if (wallet == null) {
                logger.warn("Wallet cannot be found for userId: {}", userId);
                throw new NotFoundException("Wallet not found");
            }

            logger.info("Wallet fetched for userId: {}", userId);

            return Map.of(
                    "walletId", wallet.getId(),
                    "balance", wallet.getBalance(),
                    "status", wallet.getStatus().name()
            );

        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching wallet for userId: {}", userId, e);
            throw new InternalServerException("Database error");
        }
    }

    public Map<String, Object> transfer(Long senderUserId,
                                        Long receiverUserId,
                                        BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Amount must be positive : {}", amount);
            throw new BadRequestException("Amount must be positive");
        }

        try (Connection connection = dataSource.getConnection()) {

            connection.setAutoCommit(false);

            Long transactionId = null;

            try {

                Wallet sender = walletDao.findByUserIdForUpdate(connection, senderUserId);

                Wallet receiver = walletDao.findByUserIdForUpdate(connection, receiverUserId);

                if (sender == null || receiver == null) {
                    logger.warn("Wallet not found for either sender id: {} or receiver id: {}",senderUserId,receiverUserId);
                    throw new NotFoundException("Wallet not found");
                }


                transactionId = transactionDao.createTransaction(connection,
                        sender.getId(),
                        receiver.getId(),
                        amount,
                        TransactionType.TRANSFER
                );

                if (sender.getBalance().compareTo(amount) < 0) {

                    transactionDao.updateStatus(connection,
                            transactionId,
                            TransactionStatus.FAILED.name()
                    );
                    logger.warn("Sender's wallet amount is not Sufficient for transfer for this amount: {}",amount);
                    throw new BadRequestException("Insufficient balance");
                }


                walletDao.updateBalance(connection, sender.getId(), sender.getBalance().subtract(amount));

                walletDao.updateBalance(connection, receiver.getId(), receiver.getBalance().add(amount));

                transactionDao.updateStatus(connection, transactionId, TransactionStatus.SUCCESS.name());

                connection.commit();

                logger.info("Transaction successful for ID: {}",transactionId);
                return Map.of(
                        "message", "Transfer successful",
                        "transactionId", transactionId
                );

            } catch (Exception e) {

                if (transactionId != null) {
                    transactionDao.updateStatus(connection, transactionId, TransactionStatus.FAILED.name());
                    logger.error("Transaction failed for id: {}",transactionId, e);
                }
                connection.rollback();
                throw e;
            }

        } catch (Exception e) {
            logger.error("Transaction failed due to internal server error", e);
            throw new InternalServerException("Transfer failed");
        }
    }


}