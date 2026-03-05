package com.fintech.Filter;

import com.fintech.Exception.ForbiddenException;
import com.fintech.Exception.UnauthorizedException;
import com.fintech.Model.Entity.User;
import com.fintech.Model.Enum.UserRole;
import com.fintech.Service.AuthService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.IOException;

public class AdminAuthorizationFilter implements Filter {
    private AuthService authService;

    @Override
    public void init(FilterConfig filterConfig) {
        ServletContext servletContext = filterConfig.getServletContext();
        DataSource dataSource=(DataSource) servletContext.getAttribute("dataSource");
        this.authService=new AuthService(dataSource);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        Long userId= (Long) req.getAttribute("userId");
        User user=authService.findById(userId);
        UserRole role=user.getRole();

        if (role == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        if (!role.equals(UserRole.ADMIN)) {
            throw new ForbiddenException("Access Denied");
        }

        chain.doFilter(request, response);
    }
}
