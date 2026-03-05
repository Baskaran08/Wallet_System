package com.fintech.Controller;


import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fintech.Dto.LoginRequest;
import com.fintech.Dto.RegisterRequest;
import com.fintech.Exception.BadRequestException;
import com.fintech.Service.AuthService;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.*;
import java.io.IOException;
import java.util.Map;

public class AuthController extends HttpServlet {

    private AuthService authService;
    private ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Override
    public void init() {
        HikariDataSource dataSource = (HikariDataSource) getServletContext().getAttribute("dataSource");
        this.authService = new AuthService(dataSource);
        objectMapper = new ObjectMapper();
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException  {
        String path = request.getPathInfo();

        if ("/register".equals(path)) {
            handleRegister(request, response);
        }
        else if ("/login".equals(path)) {
            handleLogin(request,response);
        } else {
            logger.error("Invalid Auth path for POST Method: {}", path);
            throw new BadRequestException("Invalid auth path");
        }

    }

    private void handleLogin(HttpServletRequest request,
                             HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        LoginRequest loginRequest = objectMapper.readValue(request.getReader(), LoginRequest.class);

        String token = authService.login(loginRequest.getEmail(), loginRequest.getPassword());

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(
                response.getWriter(),
                Map.of(
                        "message", "Login successful",
                        "token", token
                ));
    }

    private void handleRegister(HttpServletRequest request,
                                HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        RegisterRequest registerRequest =
                objectMapper.readValue(request.getReader(), RegisterRequest.class);

        authService.register(registerRequest.getFullName(), registerRequest.getEmail(), registerRequest.getPassword());

        response.setStatus(HttpServletResponse.SC_CREATED);

        objectMapper.writeValue(response.getWriter(), Map.of("message", "User registered successfully"));
    }
}