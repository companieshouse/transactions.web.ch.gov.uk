package uk.gov.companieshouse.transactions.web.utils.constants;

public final class UrlConstants {

    // Route served by this application, used in @RequestMapping; not an external
    // URI, so it should not be configurable
    @SuppressWarnings("java:S1075")
    public static final String WHAT_IS_YOUR_ROLE_PATH = "/transaction/{transactionId}/presenter/what-is-your-role";

    @SuppressWarnings("java:S1075")
    public static final String CONFIRM_STATEMENTS_PATH = "/transaction/{transactionId}/presenter/confirmstatements";

    // To be filled later
    public static final String CONFIRM_STATEMENTS_NEXT_PAGE_PATH = "next/page";

    private UrlConstants() {
    }
}
