package uk.gov.companieshouse.transactions.web.service.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenServiceTests {

    private static final String SECRET = "chs-secret-chs-secret-chs-secret-chs-secret-chs-secret-0123456789";
    private static final String OTHER_SECRET = "web-secret-web-secret-web-secret-web-secret-web-secret-0123456789";
    private static final String TRANSACTION_ID = "123456-654321-123456";

    private final TokenService tokenService = new TokenService(SECRET, 30);

    @Test
    @DisplayName("verifyChsToken returns the claims of a valid token for the transaction")
    void verifyChsTokenValid() {
        JWTClaimsSet claims = tokenService.verifyChsToken(
                sign(SECRET, JWSAlgorithm.HS256, claims("AD01", TRANSACTION_ID, null, future())), TRANSACTION_ID);

        assertNotNull(claims);
        assertEquals("AD01", TokenService.getStringClaim(claims, "form_type"));
    }

    @Test
    @DisplayName("verifyChsToken returns null for a token for a different transaction")
    void verifyChsTokenWrongTransaction() {
        assertNull(tokenService.verifyChsToken(chsToken("AD01", "other", future()), TRANSACTION_ID));
    }

    @Test
    @DisplayName("verifyChsToken returns null when the form type is missing")
    void verifyChsTokenNoFormType() {
        assertNull(tokenService.verifyChsToken(chsToken(null, TRANSACTION_ID, future()), TRANSACTION_ID));
    }

    @Test
    @DisplayName("verifyChsToken returns null for a token signed with a different secret")
    void verifyChsTokenWrongSecret() {
        String token = sign(OTHER_SECRET, JWSAlgorithm.HS256, claims("AD01", TRANSACTION_ID, null, future()));

        assertNull(tokenService.verifyChsToken(token, TRANSACTION_ID));
    }

    @Test
    @DisplayName("verifyChsToken returns null for an expired token")
    void verifyChsTokenExpired() {
        Date past = new Date(System.currentTimeMillis() - 60_000);

        assertNull(tokenService.verifyChsToken(chsToken("AD01", TRANSACTION_ID, past), TRANSACTION_ID));
    }

    @Test
    @DisplayName("verifyChsToken returns null for a token with no expiry")
    void verifyChsTokenNoExpiry() {
        assertNull(tokenService.verifyChsToken(chsToken("AD01", TRANSACTION_ID, null), TRANSACTION_ID));
    }

    @Test
    @DisplayName("verifyChsToken returns null for a token signed with a different algorithm")
    void verifyChsTokenWrongAlgorithm() {
        String token = sign(SECRET, JWSAlgorithm.HS512, claims("AD01", TRANSACTION_ID, null, future()));

        assertNull(tokenService.verifyChsToken(token, TRANSACTION_ID));
    }

    @Test
    @DisplayName("verifyChsToken returns null for missing, malformed or unsigned input")
    void verifyChsTokenInvalidInput() {
        assertNull(tokenService.verifyChsToken(null, TRANSACTION_ID));
        assertNull(tokenService.verifyChsToken(" ", TRANSACTION_ID));
        assertNull(tokenService.verifyChsToken("not-a-token", TRANSACTION_ID));
        assertNull(tokenService.verifyChsToken("eyJhbGciOiJub25lIn0.e30.", TRANSACTION_ID));
    }

    @Test
    @DisplayName("verifyChsToken returns null when no secret is configured")
    void verifyChsTokenNoSecretConfigured() {
        TokenService noSecret = new TokenService("", 30);

        assertNull(noSecret.verifyChsToken(chsToken("AD01", TRANSACTION_ID, future()), TRANSACTION_ID));
    }

    @Test
    @DisplayName("created token carries the incoming claims and role and verifies for the same transaction")
    void createdTokenVerifies() {
        JWTClaimsSet incoming = new JWTClaimsSet.Builder()
                .claim("form_type", "AD01")
                .claim("transaction_id", TRANSACTION_ID)
                .claim("return_url", "/return")
                .claim("back_url", "/back")
                .claim("lang", "cy")
                .claim("unrelated", "dropped")
                .build();

        String token = tokenService.createTransactionsWebToken(incoming, "owner");
        JWTClaimsSet claims = tokenService.verifyTransactionsWebToken(token, TRANSACTION_ID);

        assertNotNull(claims);
        assertEquals("owner", TokenService.getStringClaim(claims, "role"));
        assertEquals("AD01", TokenService.getStringClaim(claims, "form_type"));
        assertEquals("/return", TokenService.getStringClaim(claims, "return_url"));
        assertEquals("/back", TokenService.getStringClaim(claims, "back_url"));
        assertEquals("cy", TokenService.getStringClaim(claims, "lang"));
        assertNull(claims.getClaim("unrelated"));
        assertTrue(claims.getExpirationTime().after(new Date()));
    }

    @Test
    @DisplayName("verifyTransactionsWebToken returns null for a token for a different transaction")
    void verifyTransactionsWebTokenWrongTransaction() {
        String token = tokenService.createTransactionsWebToken(claims("AD01", TRANSACTION_ID, null, null), "owner");

        assertNull(tokenService.verifyTransactionsWebToken(token, "other"));
    }

    @Test
    @DisplayName("verifyTransactionsWebToken returns null when the role is missing")
    void verifyTransactionsWebTokenNoRole() {
        String token = sign(SECRET, JWSAlgorithm.HS256, claims("AD01", TRANSACTION_ID, null, future()));

        assertNull(tokenService.verifyTransactionsWebToken(token, TRANSACTION_ID));
    }

    @Test
    @DisplayName("verifyTransactionsWebToken returns null for a token signed with a different secret")
    void verifyTransactionsWebTokenWrongSecret() {
        String token = sign(OTHER_SECRET, JWSAlgorithm.HS256, claims("AD01", TRANSACTION_ID, "owner", future()));

        assertNull(tokenService.verifyTransactionsWebToken(token, TRANSACTION_ID));
    }

    @Test
    @DisplayName("createTransactionsWebToken returns null when the secret is missing or too short")
    void createTokenUnusableSecret() {
        JWTClaimsSet incoming = claims("AD01", TRANSACTION_ID, null, null);

        assertNull(new TokenService("", 30).createTransactionsWebToken(incoming, "owner"));
        assertNull(new TokenService("too-short", 30).createTransactionsWebToken(incoming, "owner"));
    }

    @Test
    @DisplayName("getStringClaim returns null for a claim that is not a string")
    void getStringClaimNotString() {
        JWTClaimsSet claims = new JWTClaimsSet.Builder().claim("number", 1).build();

        assertNull(TokenService.getStringClaim(claims, "number"));
        assertNull(TokenService.getStringClaim(claims, "missing"));
    }

    private static String chsToken(String formType, String transactionId, Date expiry) {
        return sign(SECRET, JWSAlgorithm.HS256, claims(formType, transactionId, null, expiry));
    }

    private static Date future() {
        return new Date(System.currentTimeMillis() + 60_000);
    }

    private static JWTClaimsSet claims(String formType, String transactionId, String role, Date expiry) {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder().claim("transaction_id", transactionId);
        if (formType != null) {
            builder.claim("form_type", formType);
        }
        if (role != null) {
            builder.claim("role", role);
        }
        if (expiry != null) {
            builder.expirationTime(expiry);
        }
        return builder.build();
    }

    private static String sign(String secret, JWSAlgorithm algorithm, JWTClaimsSet claims) {
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader(algorithm), claims);
            jwt.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));
            return jwt.serialize();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
