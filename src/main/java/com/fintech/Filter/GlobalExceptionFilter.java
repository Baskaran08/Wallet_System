package com.fintech.Filter;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fintech.Dto.ErrorResponse;
import com.fintech.Exception.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

public class GlobalExceptionFilter implements Filter {

    private  ObjectMapper objectMapper;
    private Logger log;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        objectMapper = new ObjectMapper();
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        log= LoggerFactory.getLogger(GlobalExceptionFilter.class);
    }



    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        httpResponse.setContentType("application/json");
        httpResponse.setCharacterEncoding("UTF-8");
        String method = null;
        String uri = null;
        long timeTaken = 0;
        int status=0;
        long start = System.currentTimeMillis();

        try {


            method = httpRequest.getMethod();
            uri = httpRequest.getRequestURI();

            log.info("REQUEST  | method={} uri={} ", method, uri);

            // DO FILTER
            chain.doFilter(request, response);

            timeTaken = System.currentTimeMillis() - start;
            status = httpResponse.getStatus();

            log.info("RESPONSE | method={} uri={} status={} timeTaken={}ms",
                    method, uri, status, timeTaken);

        } catch (AppException ex) {

            timeTaken = System.currentTimeMillis() - start;
            status=ex.getStatusCode();
            httpResponse.setStatus(ex.getStatusCode());
            httpResponse.setContentType("application/json");

            log.error("RESPONSE | method={} uri={} status={} timeTaken={}ms error={}",
                    method, uri, status, timeTaken, ex.getMessage());

            ErrorResponse errorResponse =
                    new ErrorResponse(
                            LocalDateTime.now(),
                            ex.getStatusCode(),
                            getErrorName(ex.getStatusCode()),
                            ex.getMessage(),
                            httpRequest.getRequestURI()
                    );

            objectMapper.writeValue(httpResponse.getWriter(), errorResponse);

        } catch (Exception ex) {

            timeTaken = System.currentTimeMillis() - start;
            status=500;
            httpResponse.setStatus(500);
            httpResponse.setContentType("application/json");

            log.error("RESPONSE | method={} uri={} status={} timeTaken={}ms error={}",
                    method, uri, status, timeTaken, ex.getMessage());

            ErrorResponse errorResponse =
                    new ErrorResponse(
                            LocalDateTime.now(),
                            500,
                            "Internal Server Error",
                            "Unexpected error occurred",
                            httpRequest.getRequestURI()
                    );

            objectMapper.writeValue(httpResponse.getWriter(), errorResponse);
        }
    }

    private String getErrorName(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            default -> "Error";
        };
    }
}