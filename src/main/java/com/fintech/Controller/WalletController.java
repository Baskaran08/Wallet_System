package com.fintech.Controller;


import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fintech.Dto.TransferRequest;
import com.fintech.Exception.BadRequestException;
import com.fintech.Service.WalletService;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;
import java.util.Map;

public class WalletController extends HttpServlet {

    private WalletService walletService;
    private  ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);


    @Override
    public void init() {
        HikariDataSource dataSource = (HikariDataSource) getServletContext().getAttribute("dataSource");
        walletService = new WalletService(dataSource);
        objectMapper = new ObjectMapper();
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String path = request.getPathInfo();


        if(path.equals("/me")){
            handleGetMyWallet(request, response);
        }
        else{
            logger.error("Invalid Wallet path for GET Method: {}", path);
            throw new BadRequestException("Invalid wallet path");
        }


    }

    @Override
    protected void doPost(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        String path = request.getPathInfo();


        if(path.equals("/transfer")){
            handleTransfer(request,response);
        }
        else{
            logger.error("Invalid Wallet path for POST Method: {}", path);
            throw new BadRequestException("Invalid wallet path");
        }


    }

    private void handleGetMyWallet(HttpServletRequest request,
                                   HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");

        Long userId = (Long) request.getAttribute("userId");

        if (userId == null) {
            throw new BadRequestException("User not authenticated");
        }

        Map<String, Object> walletData =
                walletService.getWalletByUserId(userId);

        objectMapper.writeValue(response.getWriter(), walletData);
    }

    private void handleTransfer(HttpServletRequest request,
                                HttpServletResponse response)
            throws IOException {

        Long senderUserId =
                (Long) request.getAttribute("userId");

        TransferRequest req =
                objectMapper.readValue(request.getReader(),
                        TransferRequest.class);

        Map<String, Object> result =
                walletService.transfer(
                        senderUserId,
                        req.getReceiverId(),
                        req.getAmount()
                );

        objectMapper.writeValue(response.getWriter(), result);
    }
}