package com.fintech.Service;
import com.fintech.Controller.AdminController;
import com.fintech.Dao.TransactionDao;
import com.fintech.Dto.PaginatedResponse;
import com.fintech.Model.Entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

public class TransactionService{

    private final TransactionDao transactionDao;
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);



    public TransactionService(DataSource dataSource) {
        this.transactionDao=new TransactionDao(dataSource);
    }

    public PaginatedResponse<Transaction> getUserTransactions(Long walletId, int page, int size) {
        int offset = (page-1) * size;

        List<Transaction> data = transactionDao.findByWalletId(walletId, offset, size);

        int total = transactionDao.countByWalletId(walletId);

        logger.info("All transaction is successfully retrieved for wallet ID: {}",walletId);
        return new PaginatedResponse<>(data, page, size, total);
    }


}