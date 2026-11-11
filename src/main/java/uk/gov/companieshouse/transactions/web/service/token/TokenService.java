package uk.gov.companieshouse.transactions.web.service.token;

import static uk.gov.companieshouse.transactions.web.utils.constants.TokenClaims.CARRIED_CLAIMS;
import static uk.gov.companieshouse.transactions.web.utils.constants.TokenClaims.FORM_TYPE;
import static uk.gov.companieshouse.transactions.web.utils.constants.TokenClaims.ROLE;
import static uk.gov.companieshouse.transactions.web.utils.constants.TokenClaims.TRANSACTION_ID;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.transactions.web.TransactionsWebApplication;

/**
 * Verifies the token received from the CHS journey, and issues and verifies the
 * token this service passes between its own pages. The two use different
 * secrets so a token from one cannot be accepted as the other.
 */
@Service
public class TokenService {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionsWebApplication.APPLICATION_NAME_SPACE);

    private static final String ISSUER = "transactions-web";

    private final String chsSecret;

    private final String transactionsWebSecret;

    private final Duration lifetime;

    public TokenService(@Value("${jwt.signing.key}") String chsSecret,
            @Value("${transactions.web.jwt.secret}") String transactionsWebSecret,
            @Value("${transactions.web.jwt.lifetime-minutes:30}") long lifetimeMinutes) {
        this.chsSecret = chsSecret;
        this.transactionsWebSecret = transactionsWebSecret;
        this.lifetime = Duration.ofMinutes(lifetimeMinutes);
    }

    /**
     * Verifies a token issued by the CHS journey. Returns its claims, or
     * {@code null} if it is invalid, has no form type, or is for a different
     * transaction.
     */
    public JWTClaimsSet verifyChsToken(String token, String transactionId) {
        return checkClaims(verifySignature(token, chsSecret), transactionId, false);
    }

    /**
     * Verifies a token issued by this service. Returns its claims, or {@code null}
     * if it is invalid, has no form type or role, or is for a different
     * transaction.
     */
    public JWTClaimsSet verifyTransactionsWebToken(String token, String transactionId) {
        return checkClaims(verifySignature(token, transactionsWebSecret), transactionId, true);
    }

    /**
     * Issues a token carrying the claims of the incoming CHS token plus the chosen
     * role. Returns {@code null} if the token could not be signed.
     */
    public String createTransactionsWebToken(JWTClaimsSet chsClaims, String role) {
        Date now = new Date();

        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .issueTime(now)
                .expirationTime(new Date(now.getTime() + lifetime.toMillis()))
                .claim(ROLE, role);

        CARRIED_CLAIMS.forEach(name -> {
            Object value = chsClaims.getClaim(name);
            if (value != null) {
                builder.claim(name, value);
            }
        });

        return sign(builder.build(), transactionsWebSecret);
    }

    public static String getStringClaim(JWTClaimsSet claims, String name) {
        Object value = claims.getClaim(name);
        return value instanceof String string ? string : null;
    }

    /**
     * Returns the claims of an HS256-signed token, or {@code null} if the token is
     * malformed, uses a different algorithm, has an invalid signature, or has
     * expired.
     */
    private JWTClaimsSet verifySignature(String token, String secret) {
        if (isBlank(token)) {
            return null;
        }

        if (isBlank(secret)) {
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

    /**
     * Signs the claims with HS256 and returns the serialised token, or
     * {@code null} if the secret is missing or unusable (e.g. shorter than 256
     * bits).
     */
    private String sign(JWTClaimsSet claims, String secret) {
        if (isBlank(secret)) {
            LOG.error("JWT not created: no signing secret configured", new HashMap<>());
            return null;
        }

        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));
            return jwt.serialize();
        } catch (JOSEException ex) {
            LOG.error("JWT not created: signing failed", new HashMap<>());
            return null;
        }
    }

    private JWTClaimsSet checkClaims(JWTClaimsSet claims, String transactionId, boolean requireRole) {
        if (claims == null) {
            return null;
        }

        if (isBlank(getStringClaim(claims, FORM_TYPE))
                || (requireRole && isBlank(getStringClaim(claims, ROLE)))) {
            LOG.error("JWT rejected: required claims are missing", new HashMap<>());
            return null;
        }

        if (transactionId == null || !transactionId.equals(getStringClaim(claims, TRANSACTION_ID))) {
            LOG.error("JWT rejected: transaction id does not match the request", new HashMap<>());
            return null;
        }

        return claims;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
