package uk.gov.companieshouse.transactions.web.service.presenter;

import uk.gov.companieshouse.transactions.web.model.presenter.PresenterApiError;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterApiResponse;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterType;

/**
 * Calls the Presenter API to obtain identity verification information and
 * applicable filing statements for the given presenter type.
 */
public interface PresenterApiService {

    /**
     * Represents a successful API call.
     */
    record Success(PresenterApiResponse response) implements PresenterApiResult {}

    /**
     * Represents a failed API call with a specific error reason.
     */
    record Failure(PresenterApiError error) implements PresenterApiResult {}

    /**
     * Sealed marker interface for the result of calling the Presenter API.
     */
    sealed interface PresenterApiResult permits Success, Failure {}

    /**
     * Calls the Presenter API for the given transaction context and presenter type.
     *
     * @param transactionId the ID of the current transaction
     * @param companyNumber the company number associated with the transaction
     * @param formType      the form type derived from the transaction description
     * @param chsUserId     the CHS user ID obtained from the session
     * @param presenterType the filer type selected by the user
     * @return a {@link PresenterApiResult} — either {@link Success} or {@link Failure}
     */
    PresenterApiResult call(String transactionId, String companyNumber, String formType,
            String chsUserId, PresenterType presenterType);
}
