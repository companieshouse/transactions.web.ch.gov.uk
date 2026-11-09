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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserId() {

        Map<String, Object> sessionData = getSessionDataFromContext();

        if (sessionData != null
                && sessionData.get("signin_info") instanceof Map<?, ?> signInInfo
                && signInInfo.get("user_profile") instanceof Map<?, ?> userProfile
                && userProfile.get("id") instanceof String userId) {
            return userId;
        }
        return null;
    }
}
