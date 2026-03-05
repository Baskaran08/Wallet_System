package com.fintech.Controller;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
;
import com.fintech.Dto.PaginatedResponse;
import com.fintech.Exception.BadRequestException;
import com.fintech.Model.Entity.Transaction;
import com.fintech.Model.Entity.Wallet;
import com.fintech.Service.TransactionService;
import com.fintech.Service.WalletService;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;


public class TransactionController extends HttpServlet {

    private TransactionService transactionService;
    private ObjectMapper objectMapper;
    private WalletService walletService;
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);



    @Override
    public void init() {
        HikariDataSource dataSource = (HikariDataSource) getServletContext().getAttribute("dataSource");
        this.transactionService = new TransactionService(dataSource);
        this.walletService=new WalletService(dataSource);
        objectMapper = new ObjectMapper();
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();

        if(path.equals("/my")){
            handlePaginatedTransaction(req, resp);
        }
        else {
            logger.error("Invalid Transaction path for GET Method: {}", path);
            throw new BadRequestException("Invalid Transaction path");
        }
    }

    private void handlePaginatedTransaction(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        Long userId = (Long) req.getAttribute("userId");

        int page = Integer.parseInt(req.getParameter("page") == null ? "1" : req.getParameter("page"));

        int size = Integer.parseInt(req.getParameter("size") == null ? "10" : req.getParameter("size"));

        Map<String, Object> walletData = walletService.getWalletByUserId(userId);
        Long walletId= (Long) walletData.get("walletId");

        PaginatedResponse<Transaction> result = transactionService.getUserTransactions(walletId, page, size);

        resp.setContentType("application/json");
        objectMapper.writeValue(resp.getWriter(),result);
    }
}
