package com.fintech.Filter;

import com.fintech.Exception.UnauthorizedException;
import com.fintech.Util.JwtUtil;
import io.jsonwebtoken.Claims;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class JwtAuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI();

        // Public endpoints
        if (path.startsWith("/auth")) {
            chain.doFilter(request, response);
            return;
        }

        String header = req.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid token");
        }

        String token = header.substring(7);

        try {

            Claims claims = JwtUtil.validateToken(token);
            Long userId = ((Integer) claims.get("userId")).longValue();

            req.setAttribute("userId", userId);

        } catch (Exception e) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        chain.doFilter(request, response);
    }
}