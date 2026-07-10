package uk.gov.companieshouse.transactions.web.api;

import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;

/**
 * The {@code ApiClientService} interface provides an abstraction that can be
 * used when testing {@code ApiClientManager} static methods, without imposing
 * the use of a test framework that supports mocking of static methods.
 */
public interface ApiClientService {

    /**
     * Returns an {@link ApiClient} with which to execute API requests
     * @return an ApiClient
     */
    ApiClient getApiClient();

    /**
     * Returns an {@link InternalApiClient} for internal/private API calls
     * @return an InternalApiClient
     */
    InternalApiClient getInternalApiClient();
}
