package uk.gov.companieshouse.transactions.web.utils.jwthelpers;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.transactions.web.TransactionsWebApplication;

public final class JwtHelpers {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionsWebApplication.APPLICATION_NAME_SPACE);

    private JwtHelpers() {
    }

    public static final JWTClaimsSet readClaims(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            return JWTParser.parse(token).getJWTClaimsSet();
        } catch (ParseException ex) {
            return null;
        }
    }

    /**
     * Returns the claims of an HS256-signed token, or {@code null} if the token is
     * malformed, uses a different algorithm, has an invalid signature, or has
     * expired. {@code secret} is the shared HMAC key.
     */
    public static final JWTClaimsSet verifyToken(String token, String secret) {
        if (token == null || token.isBlank()) {
            return null;
        }

        if (secret == null || secret.isBlank()) {
            LOG.error("JWT rejected: no signing secret configured", new HashMap<>());
            return null;
        }

        try {
            SignedJWT jwt = SignedJWT.parse(token);

            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())) {
                LOG.error("JWT rejected: unexpected signing algorithm", new HashMap<>());
                return null;
            }

            if (!jwt.verify(new MACVerifier(secret.getBytes(StandardCharsets.UTF_8)))) {
                LOG.error("JWT rejected: signature verification failed", new HashMap<>());
                return null;
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date expiry = claims.getExpirationTime();

            if (expiry == null || !expiry.after(new Date())) {
                LOG.error("JWT rejected: token has no expiry or has expired", new HashMap<>());
                return null;
            }

            return claims;
        } catch (ParseException | JOSEException ex) {
            LOG.error("JWT rejected: token could not be parsed or verified", new HashMap<>());
            return null;
        }
    }

    public static final String getStringClaim(JWTClaimsSet claims, String name) {
        Object value = claims.getClaim(name);
        return value instanceof String string ? string : null;
    }
}
