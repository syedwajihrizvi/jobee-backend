package com.rizvi.jobee.filters;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.services.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var authHeader = request.getHeader("x-auth-token");
        System.out.println(authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No JWT token found in request header");
            filterChain.doFilter(request, response);
            return;
        }
        var token = authHeader.replace("Bearer", "").trim();
        if (!jwtService.validateToken(token)) {
            System.out.println("Invalid JWT token: " + token);
            filterChain.doFilter(request, response);
            return;
        }
        String email = jwtService.getEmailFromToken(token);
        Long id = jwtService.getIdFromToken(token);
        String role = jwtService.getRoleFromToken(token);
        CustomPrincipal principal = new CustomPrincipal(id, email, role);
        var authorites = List.of(new SimpleGrantedAuthority(role));
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorites);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
        return;
    }

}
