package com.rizvi.jobee.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.rizvi.jobee.enums.Roles;
import com.rizvi.jobee.filters.JwtFilter;
import com.rizvi.jobee.services.AccountService;

import lombok.AllArgsConstructor;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final AccountService accountService;
    private final JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider(accountService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // TODO: Fix all endpoints to use the correct roles
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(c -> c.disable())
                .authorizeHttpRequests(c -> c
                        .requestMatchers(HttpMethod.GET, "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/business-accounts/register", "/business-accounts/login")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/accounts/register", "/accounts/login")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/user-documents").permitAll()
                        .requestMatchers(HttpMethod.GET, "/profiles/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/profiles/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/jobs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/interviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/applications").permitAll()
                        .requestMatchers(HttpMethod.POST, "/jobs").hasAuthority(Roles.BUSINESS.name())
                        .requestMatchers(HttpMethod.DELETE, "/jobs/**").hasAuthority(Roles.BUSINESS.name())
                        .requestMatchers(HttpMethod.POST, "/applications").hasAuthority(Roles.USER.name())
                        .requestMatchers(HttpMethod.POST, "/interviews").hasAuthority(Roles.BUSINESS.name()))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(c -> {
                    c.authenticationEntryPoint(
                            new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
                    c.accessDeniedHandler(
                            (_, response, _) -> response.setStatus(HttpStatus.FORBIDDEN.value()));
                });
        return http.build();
    }
}
