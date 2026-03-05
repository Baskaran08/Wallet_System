package com.fintech.Service;

import com.fintech.Dao.TransactionDao;
import com.fintech.Dao.WalletDao;
import com.fintech.Dto.PaginatedResponse;
import com.fintech.Exception.InternalServerException;
import com.fintech.Exception.NotFoundException;
import com.fintech.Model.Entity.Transaction;
import com.fintech.Model.Entity.Wallet;
import com.fintech.Model.Enum.TransactionStatus;
import com.fintech.Model.Enum.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final DataSource dataSource;
    private final WalletDao walletDao;
    private final TransactionDao transactionDao;

    public AdminService(DataSource dataSource) {
        this.dataSource = dataSource;
        this.walletDao = new WalletDao(dataSource);
        this.transactionDao=new TransactionDao(dataSource);
    }

    public PaginatedResponse<Transaction> getAllTransactions(int page, int size) {

        int offset = (page-1) * size;

        List<Transaction> data = transactionDao.findAll(offset, size);

        int total = transactionDao.countAll();

        logger.info("All transaction is successfully retrieved for all users ");
        return new PaginatedResponse<>(data, page, size, total);
    }

    public Map<String, Object> handleDeposit(Long walletId, BigDecimal amount) {
        try(Connection connection = dataSource.getConnection())  {

            connection.setAutoCommit(false);
            Long transactionId = null;

            try{
                Wallet wallet = walletDao.findByWalletId( connection, walletId);

                if (wallet == null) {
                    logger.warn("Wallet not found for ID: {}",walletId);
                    throw new NotFoundException("Wallet not found");
                }

                transactionId = transactionDao.createTransactionForDeposit(connection,
                        walletId,
                        amount,
                        TransactionType.DEPOSIT
                );

                walletDao.updateBalance(connection, walletId, wallet.getBalance().add(amount));

                transactionDao.updateStatus(connection, transactionId, TransactionStatus.SUCCESS.name());

                connection.commit();

                logger.info("Amount is successfully deposited to wallet ID: {}, transaction ID: {}",walletId,transactionId);
                return Map.of(
                        "message", "Deposit successful",
                        "transactionId", transactionId
                );


            }
            catch (Exception e){
                if (transactionId != null) {
                    transactionDao.updateStatus(connection, transactionId, TransactionStatus.FAILED.name());
                    logger.error("Transaction id {} failed during deposit to wallet ID: {} ",transactionId,walletId, e);
                }
                connection.rollback();
                throw e;
            }
        } catch (Exception e) {
            logger.error("Error during wallet deposit for walletId: {}", walletId, e);
            throw new InternalServerException("Database error");
        }
    }
}
