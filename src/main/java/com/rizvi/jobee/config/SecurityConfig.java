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

import com.rizvi.jobee.enums.Role;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(c -> c.disable())
                .authorizeHttpRequests(c -> c
                        .requestMatchers(HttpMethod.GET, "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/business-accounts/register",
                                "/api/business-accounts/login",
                                "/api/business-accounts/register-via-code")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/accounts/register", "/api/accounts/login")
                        .permitAll()
                        .requestMatchers("/ws-chat/**").permitAll()
                        .requestMatchers("/ws-notifications/**").permitAll()
                        .requestMatchers("/app/**").permitAll()
                        .requestMatchers("/topic/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/oauth2/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/messages/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/user-notifications").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/user-notifications/mark-all-read").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/user-notifications/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/user-notifications/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/messages/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/messages/conversations").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/companies/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/companies/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/user-documents/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/profile/experiences/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/business-accounts/invite-member").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/business-accounts/me").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/business-profiles").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/business-profiles/socialMedia/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/business-profiles/socialMedia").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/business-profiles/dashboard").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/business-profiles/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/user-documents").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/user-documents/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/user-documents/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/user-documents/link").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/user-documents/image").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/profiles/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/profiles/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/profiles/socialMedia/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/profiles/views").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/profiles/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/profiles/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/profiles/skills/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/profiles/skills").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/profiles/projects").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/profiles/projects").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/profiles/projects/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/profiles/views").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/profiles/projects/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/profiles/skills/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/profiles/favorite-jobs").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/profiles/education").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/profiles/education").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/profiles/education/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/profiles/experiences/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/profiles/favorite-company").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/profiles/update-primary-resume").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/jobs/companies/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/jobs").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/jobs/generate-ai-description").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/jobs/*/views").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/jobs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/jobs/favorites").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/interviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/interviews/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/interviews/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/interviews/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/applications").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/applications/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/applications/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/jobs").hasAuthority(Role.BUSINESS.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/jobs/**").hasAuthority(Role.BUSINESS.name())
                        .requestMatchers(HttpMethod.POST, "/api/applications/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/interviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/interviews/**").permitAll())
                // .requestMatchers(HttpMethod.POST,
                // "/interviews").hasAuthority(Roles.BUSINESS.name()))
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
