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

    /**
     * Returns the id of the signed-in user held in the current session.
     *
     * @return the user id, or {@code null} if it is absent or the session data is malformed
     */
    String getUserId();
}
