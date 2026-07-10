package uk.gov.companieshouse.transactions.web.service.transaction.impl;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.PresenterData;
import uk.gov.companieshouse.api.model.transaction.IndividualUser;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.transactions.web.api.ApiClientService;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterApiResponse;
import uk.gov.companieshouse.transactions.web.service.transaction.TransactionsService;

@Service
public class TransactionsServiceImpl implements TransactionsService {

    final ApiClientService apiClientService;

    public TransactionsServiceImpl(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction getTransaction(String transactionId) throws ServiceException {

        ApiClient apiClient = apiClientService.getApiClient();

        try {
            return apiClient.transactions().get("/transactions/" + transactionId).execute().getData();

        } catch (ApiErrorResponseException e) {
            throw new ServiceException("Error retrieving transaction", e);
        } catch (URIValidationException e) {
            throw new ServiceException("Invalid URI for transaction resource", e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTransactionClosedOrClosedPendingPayment(Transaction transaction) {

        return transaction.getStatus() == TransactionStatus.CLOSED || transaction.getStatus() == TransactionStatus.CLOSED_PENDING_PAYMENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void patchPresenterData(String transactionId, PresenterApiResponse presenterResponse) throws ServiceException {

        String uri = "/transactions/" + transactionId + "/presenter";

        PresenterData presenterData = new PresenterData();
        presenterData.setStatements(presenterResponse.getStatements());

        if (presenterResponse.getIndividualUser() != null) {
            IndividualUser individualUser = new IndividualUser(
                    presenterResponse.getIndividualUser().getUvid(),
                    presenterResponse.getIndividualUser().getName(),
                    presenterResponse.getIndividualUser().getDob());
            presenterData.setIndividualUser(individualUser);
        }

        try {
            ApiResponse<Void> response = apiClientService.getInternalApiClient()
                    .privateTransaction()
                    .presenterPatch(uri, presenterData)
                    .execute();

            if (response.getStatusCode() != 204) {
                throw new ServiceException(
                        "Unexpected status from presenter PATCH: " + response.getStatusCode());
            }
        } catch (ApiErrorResponseException e) {
            throw new ServiceException("Error patching presenter data for transaction " + transactionId, e);
        } catch (URIValidationException e) {
            throw new ServiceException("Invalid URI for presenter PATCH: " + uri, e);
        }
    }
}
