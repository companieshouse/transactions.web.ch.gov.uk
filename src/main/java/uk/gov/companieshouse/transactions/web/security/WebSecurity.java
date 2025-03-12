package uk.gov.companieshouse.transactions.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.companieshouse.csrf.config.ChsCsrfMitigationHttpSecurityBuilder;

@EnableWebSecurity
public class WebSecurity {

    @Configuration
    @Order(1)
    public static class CompanyAccountsSecurityFilterConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return ChsCsrfMitigationHttpSecurityBuilder.configureWebCsrfMitigations(http).build();
        }
    }

}
