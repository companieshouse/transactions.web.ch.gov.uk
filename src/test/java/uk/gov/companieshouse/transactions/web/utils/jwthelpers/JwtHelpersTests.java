package uk.gov.companieshouse.transactions.web.utils.jwthelpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtHelpersTests {

    private static final String SECRET = "test-secret-test-secret-test-secret-test-secret-test-secret-0123456789";
    private static final String OTHER_SECRET = "other-secret-other-secret-other-secret-other-secret-other-secret-01";

    @Test
    @DisplayName("verifyToken returns the claims of a valid, unexpired token")
    void verifyTokenValid() throws Exception {
        JWTClaimsSet claims = JwtHelpers.verifyToken(sign(SECRET, JWSAlgorithm.HS256, future()), SECRET);

        assertNotNull(claims);
        assertEquals("AD01", JwtHelpers.getStringClaim(claims, "form-type"));
    }

    @Test
    @DisplayName("verifyToken returns null for a token signed with a different secret")
    void verifyTokenWrongSecret() throws Exception {
        assertNull(JwtHelpers.verifyToken(sign(OTHER_SECRET, JWSAlgorithm.HS256, future()), SECRET));
    }

    @Test
    @DisplayName("verifyToken returns null for an expired token")
    void verifyTokenExpired() throws Exception {
        Date past = new Date(System.currentTimeMillis() - 60_000);

        assertNull(JwtHelpers.verifyToken(sign(SECRET, JWSAlgorithm.HS256, past), SECRET));
    }

    @Test
    @DisplayName("verifyToken returns null for a token with no expiry")
    void verifyTokenNoExpiry() throws Exception {
        assertNull(JwtHelpers.verifyToken(sign(SECRET, JWSAlgorithm.HS256, null), SECRET));
    }

    @Test
    @DisplayName("verifyToken returns null for a token signed with a different algorithm")
    void verifyTokenWrongAlgorithm() throws Exception {
        assertNull(JwtHelpers.verifyToken(sign(SECRET, JWSAlgorithm.HS512, future()), SECRET));
    }

    @Test
    @DisplayName("verifyToken returns null for missing, malformed or unsigned input")
    void verifyTokenInvalidInput() {
        assertNull(JwtHelpers.verifyToken(null, SECRET));
        assertNull(JwtHelpers.verifyToken(" ", SECRET));
        assertNull(JwtHelpers.verifyToken("not-a-token", SECRET));
        assertNull(JwtHelpers.verifyToken("eyJhbGciOiJub25lIn0.e30.", SECRET));
    }

    @Test
    @DisplayName("verifyToken returns null when the secret is missing")
    void verifyTokenMissingSecret() throws Exception {
        String token = sign(SECRET, JWSAlgorithm.HS256, future());

        assertNull(JwtHelpers.verifyToken(token, null));
        assertNull(JwtHelpers.verifyToken(token, ""));
    }

    private static Date future() {
        return new Date(System.currentTimeMillis() + 60_000);
    }

    private static String sign(String secret, JWSAlgorithm algorithm, Date expiry) throws Exception {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder().claim("form-type", "AD01");
        if (expiry != null) {
            builder.expirationTime(expiry);
        }

        SignedJWT jwt = new SignedJWT(new JWSHeader(algorithm), builder.build());
        jwt.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));
        return jwt.serialize();
    }
}
