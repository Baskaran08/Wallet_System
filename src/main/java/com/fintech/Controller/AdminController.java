package com.fintech.Controller;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fintech.Dao.InvoiceDao;
import com.fintech.Dto.DepositRequest;
import com.fintech.Dto.PaginatedResponse;
import com.fintech.Exception.BadRequestException;
import com.fintech.Model.Entity.Transaction;
import com.fintech.Service.AdminService;
import com.fintech.Service.AuthService;
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

public class AdminController extends HttpServlet {

    private TransactionService transactionService;
    private AuthService authService;
    private WalletService walletService;
    private ObjectMapper objectMapper;
    private AdminService adminService;
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Override
    public void init() {
        HikariDataSource dataSource = (HikariDataSource) getServletContext().getAttribute("dataSource");
        this.transactionService = new TransactionService(dataSource);
        this.authService=new AuthService(dataSource);
        this.walletService=new WalletService(dataSource);
        this.adminService=new AdminService(dataSource);

        objectMapper = new ObjectMapper();
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();

        if(path.equals("/transactions")){
            handleAdminPaginatedTransaction(req, resp);
        }
        else {
            logger.error("Invalid Admin path for GET Method: {}", path);
            throw new BadRequestException("Invalid Admin path");
        }
    }

    private void handleAdminPaginatedTransaction(HttpServletRequest req, HttpServletResponse resp) throws  IOException{

        int page = Integer.parseInt(req.getParameter("page") == null ? "1" : req.getParameter("page"));

        int size = Integer.parseInt(req.getParameter("size") == null ? "20": req.getParameter("size"));

        PaginatedResponse<Transaction> result = adminService.getAllTransactions(page, size);

        resp.setContentType("application/json");
        objectMapper.writeValue(resp.getWriter(),result);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();

        if(path.startsWith("/wallet")){
            handleDeposit(req, resp);
        }
        else {
            logger.error("Invalid Admin path For PUT Method: {}", path);
            throw new BadRequestException("Invalid Admin path");
        }
    }

    private void handleDeposit(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        String[] parts=path.split("/");
        Long walletId= Long.valueOf(parts[2]);

        DepositRequest request = objectMapper.readValue(req.getReader(), DepositRequest.class);
        Map<String, Object> result = adminService.handleDeposit(walletId, request.getAmount());
        resp.setContentType("application/json");
        objectMapper.writeValue(resp.getWriter(),result);

    }

}
