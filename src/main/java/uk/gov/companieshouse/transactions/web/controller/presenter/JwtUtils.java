package uk.gov.companieshouse.transactions.web.controller.presenter;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.transactions.web.TransactionsWebApplication;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;

public class JwtUtils {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TransactionsWebApplication.APPLICATION_NAME_SPACE);

    /**
     * Query parameter carrying the presenter JWT. The token is passed from page to page
     * (rather than held in session) so every page can show the calling service's data.
     */
    static final String TOKEN_PARAM = "token";

    private static final String PRESENTER_PATH = "/transaction/{transactionId}/presenter";

    /** Claim names in the presenter redirect JWT issued by the calling service. */
    private static final String CLAIM_FORM_TYPE = "form_type";
    private static final String CLAIM_TRANSACTION_ID = "transaction_id";
    private static final String CLAIM_RETURN_URL = "return_url";
    private static final String CLAIM_BACK_URL = "back_url";
    private static final String CLAIM_LANG = "lang";

    /**
     * Verifies the HS256 signature of the supplied JWT using {@code CHS_JWT_SECRET}
     * and returns its claims. Returns {@code null} if the token is absent, malformed,
     * has an invalid signature or was issued for a different transaction.
     */
    public static JWTClaimsSet verifyToken(String jwt, String chsJwtSecret) {
        if (jwt == null || jwt.isBlank()) {
            return null;
        }

        try {
            SignedJWT signedJwt = SignedJWT.parse(jwt);

            if (signedJwt.getHeader().getAlgorithm() != JWSAlgorithm.HS256) {
                LOGGER.error("Presenter JWT rejected: unexpected signing algorithm");
                return null;
            }

            JWSVerifier verifier = new MACVerifier(chsJwtSecret.getBytes(StandardCharsets.UTF_8));
            if (!signedJwt.verify(verifier)) {
                LOGGER.error("Presenter JWT rejected: signature verification failed");
                return null;
            }

            return signedJwt.getJWTClaimsSet();
        } catch (Exception e) {
            LOGGER.error("Failed to parse or verify presenter JWT: " + e.getMessage(), e);
            return null;
        }
    }

    /** Returns the named claim, or {@code null} if it is missing or not a string. */
    public static String getStringClaim(JWTClaimsSet claims, String name) {
        try {
            return claims.getStringClaim(name);
        } catch (ParseException e) {
            return null;
        }
    }
}
