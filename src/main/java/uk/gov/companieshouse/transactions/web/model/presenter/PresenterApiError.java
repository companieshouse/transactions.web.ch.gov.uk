package uk.gov.companieshouse.transactions.web.model.presenter;

/**
 * Possible error outcomes returned by the Presenter API.
 * These drive routing to the appropriate stop screen.
 */
public enum PresenterApiError {

    /** The user's login email is not linked to any ACSP account. */
    ACSP_NOT_LINKED,

    /** The user has not completed identity verification. */
    IDENTITY_NOT_VERIFIED
}
