package uk.gov.companieshouse.transactions.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.transactions.web.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.transactions.web.interceptor.UserDetailsInterceptor;

@SpringBootApplication
public class TransactionsWebApplication implements WebMvcConfigurer {

	public static final String APPLICATION_NAME_SPACE = "transaction.web.ch.gov.uk";

	private UserDetailsInterceptor userDetailsInterceptor;
	private LoggingInterceptor loggingInterceptor;

	@Autowired
	public TransactionsWebApplication(UserDetailsInterceptor userDetailsInterceptor,
			LoggingInterceptor loggingInterceptor) {
		this.userDetailsInterceptor = userDetailsInterceptor;
		this.loggingInterceptor = loggingInterceptor;
	}

	public static void main(String[] args) {
		SpringApplication.run(TransactionsWebApplication.class, args);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {

		registry.addInterceptor(loggingInterceptor);
		registry.addInterceptor(userDetailsInterceptor);
	}
}
