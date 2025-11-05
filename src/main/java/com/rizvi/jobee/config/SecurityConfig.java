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
                        .requestMatchers(HttpMethod.POST, "/business-accounts/register", "/business-accounts/login")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/accounts/register", "/accounts/login")
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
                        .requestMatchers(HttpMethod.GET, "/user-documents/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/experiences/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/business-accounts/me").permitAll()
                        .requestMatchers(HttpMethod.GET, "/business-profiles").permitAll()
                        .requestMatchers(HttpMethod.GET, "/business-profiles/socialMedia/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/business-profiles/socialMedia").permitAll()
                        .requestMatchers(HttpMethod.GET, "/business-profiles/dashboard").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/business-profiles/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/user-documents").permitAll()
                        .requestMatchers(HttpMethod.POST, "/user-documents/link").permitAll()
                        .requestMatchers(HttpMethod.POST, "/user-documents/image").permitAll()
                        .requestMatchers(HttpMethod.GET, "/profiles/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/profiles/socialMedia/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/profiles/views").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/profiles/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/profiles/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/profiles/skills/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/profiles/skills").permitAll()
                        .requestMatchers(HttpMethod.GET, "/profiles/projects").permitAll()
                        .requestMatchers(HttpMethod.POST, "/profiles/projects").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/profiles/projects/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/profiles/views").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/profiles/projects/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/profiles/skills/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/profiles/favorite-jobs").permitAll()
                        .requestMatchers(HttpMethod.GET, "/profiles/education").permitAll()
                        .requestMatchers(HttpMethod.POST, "/profiles/education").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/profiles/education/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/profiles/experiences/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/profiles/favorite-company").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/profiles/update-primary-resume").permitAll()
                        .requestMatchers(HttpMethod.GET, "/jobs/companies/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/jobs").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/jobs/*/views").permitAll()
                        .requestMatchers(HttpMethod.GET, "/jobs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/jobs/favorites").permitAll()
                        .requestMatchers(HttpMethod.GET, "/interviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/interviews/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/interviews/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/applications").permitAll()
                        .requestMatchers(HttpMethod.GET, "/applications/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/applications/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/jobs").hasAuthority(Role.BUSINESS.name())
                        .requestMatchers(HttpMethod.DELETE, "/jobs/**").hasAuthority(Role.BUSINESS.name())
                        .requestMatchers(HttpMethod.POST, "/applications/**").permitAll()
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
