package uk.gov.companieshouse.transactions.web.utils.jwthelpers;

import java.text.ParseException;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

public final class JwtHelpers {
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

    public static final String getStringClaim(JWTClaimsSet claims, String name) {
        Object value = claims.getClaim(name);
        return value instanceof String string ? string : null;
    }
}
