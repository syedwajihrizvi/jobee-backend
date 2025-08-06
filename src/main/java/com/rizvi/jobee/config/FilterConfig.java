package com.rizvi.jobee.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.rizvi.jobee.filters.LoggingFilter;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilter() {
        FilterRegistrationBean<LoggingFilter> registerationBean = new FilterRegistrationBean<>();
        registerationBean.setFilter(new LoggingFilter());
        registerationBean.addUrlPatterns("/*"); // Apply to all URLs
        registerationBean.setOrder(1); // Set order of the filter
        return registerationBean;
    }
}
