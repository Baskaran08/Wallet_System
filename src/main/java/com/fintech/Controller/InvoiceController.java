package com.fintech.Controller;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fintech.Dto.CreateInvoiceRequest;
import com.fintech.Exception.BadRequestException;
import com.fintech.Service.InvoiceService;
import com.fintech.Service.QrService;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class InvoiceController extends HttpServlet {

    private InvoiceService invoiceService;
    private   ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);


    @Override
    public void init() {
        HikariDataSource dataSource = (HikariDataSource) getServletContext().getAttribute("dataSource");
        QrService qrService = (QrService) getServletContext().getAttribute("qrService");
        String qrPath = getServletContext().getAttribute("qrPath").toString();
        this.invoiceService = new InvoiceService(dataSource,qrService,qrPath);
        objectMapper = new ObjectMapper();
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String path = request.getPathInfo();

        if("/".equals(path) || path == null){
            handleCreateInvoice(request, response);
        }
        else if(path.equals("/pay")){
            handlePayInvoice(request,response);
        }
        else if(path.equals("/send-email")){
            handleSendEmail(request,response);
        }
        else {
            logger.error("Invalid Invoice path for POST Method: {}", path);
            throw new BadRequestException("Invalid invoice path");
        }


    }

    private void handleSendEmail(HttpServletRequest request, HttpServletResponse response) throws IOException {

            Long userId = (Long) request.getAttribute("userId");

            Long invoiceId = Long.parseLong(request.getParameter("invoiceId"));

            String email = request.getParameter("email");

            invoiceService.sendInvoiceEmail(userId, invoiceId, email);

            objectMapper.writeValue(response.getWriter(), Map.of("message","email sent"));

    }

    private void handlePayInvoice(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String paymentToken = request.getParameter("token");
        if (paymentToken == null || paymentToken.isBlank()) {
            throw new BadRequestException("Missing payment token");
        }
        Long payerId = (Long) request.getAttribute("userId");

        Map<String, Object> result = invoiceService.payInvoice(payerId, paymentToken);

        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), result);
    }

    private void handleCreateInvoice(HttpServletRequest request,
                                     HttpServletResponse response)
            throws IOException {

        Long userId = (Long) request.getAttribute("userId");

        CreateInvoiceRequest req = objectMapper.readValue(request.getInputStream(), CreateInvoiceRequest.class);

        Map<String, Object> result = invoiceService.createInvoice(
                        userId,
                        req.getAmount(),
                        req.getDescription()
                );

        objectMapper.writeValue(response.getWriter(), result);
    }
}
