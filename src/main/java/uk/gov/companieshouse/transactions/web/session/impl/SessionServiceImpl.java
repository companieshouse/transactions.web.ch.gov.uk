package uk.gov.companieshouse.transactions.web.session.impl;

import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.session.handler.SessionHandler;
import uk.gov.companieshouse.transactions.web.session.SessionService;

@Component
public class SessionServiceImpl implements SessionService {

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getSessionDataFromContext() {

        return SessionHandler.getSessionDataFromContext();
    }
}
