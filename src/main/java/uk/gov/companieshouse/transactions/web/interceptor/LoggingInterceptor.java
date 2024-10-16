package uk.gov.companieshouse.transactions.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.util.RequestLogger;
import uk.gov.companieshouse.transactions.web.TransactionsWebApplication;

@Component
public class LoggingInterceptor implements HandlerInterceptor, RequestLogger {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TransactionsWebApplication.APPLICATION_NAME_SPACE);

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {

        logStartRequestProcessing(request, LOGGER);
        return true;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable ModelAndView modelAndView) {

        logEndRequestProcessing(request, response, LOGGER);
    }
}
