package uk.gov.companieshouse.transactions.web.session.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.transactions.web.session.SessionService;

class SessionServiceImplTests {

    private static final String USER_ID = "user-123";

    private static SessionService serviceWith(Map<String, Object> sessionData) {
        return new SessionServiceImpl() {
            @Override
            public Map<String, Object> getSessionDataFromContext() {
                return sessionData;
            }
        };
    }

    @Test
    @DisplayName("getUserId returns the user id from the signed-in user's profile")
    void getUserId() {

        Map<String, Object> sessionData = Map.of("signin_info",
                Map.of("user_profile", Map.of("id", USER_ID)));

        assertEquals(USER_ID, serviceWith(sessionData).getUserId());
    }

    @Test
    @DisplayName("getUserId returns null when there is no signed-in user")
    void getUserIdWithNoUser() {

        assertNull(serviceWith(new HashMap<>()).getUserId());
    }

    @Test
    @DisplayName("getUserId returns null when the session data is malformed")
    void getUserIdWithMalformedSession() {

        assertNull(serviceWith(Map.of("signin_info", "not-a-map")).getUserId());
        assertNull(serviceWith(Map.of("signin_info", Map.of("user_profile", "x"))).getUserId());
        assertNull(serviceWith(Map.of("signin_info", Map.of("user_profile", Map.of("id", 1)))).getUserId());
    }
}
