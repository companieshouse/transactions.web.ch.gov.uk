package uk.gov.companieshouse.transactions.web.session;

import java.util.Map;

/**
 * The {@code SessionService} interface provides an abstraction that can be
 * used when testing {@code SessionHandler} static methods, without imposing
 * the use of a test framework that supports mocking of static methods.
 */
public interface SessionService {

    /**
     * Returns a map comprising data retrieved from the current session.
     *
     * @return a map of session data
     */
    Map<String, Object> getSessionDataFromContext();
}
