package uk.gov.companieshouse.transactions.web.utils.constants;

import java.util.List;

public final class TokenClaims {

    public static final String FORM_TYPE = "form_type";

    public static final String TRANSACTION_ID = "transaction_id";

    public static final String RETURN_URL = "return_url";

    public static final String BACK_URL = "back_url";

    public static final String LANG = "lang";

    public static final String ROLE = "role";

    // Claims carried from the incoming token into the token issued by this service
    public static final List<String> CARRIED_CLAIMS = List.of(FORM_TYPE, TRANSACTION_ID, RETURN_URL, BACK_URL, LANG);

    private TokenClaims() {
    }
}
