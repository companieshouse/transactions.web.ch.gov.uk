package uk.gov.companieshouse.transactions.web.api;

import uk.gov.companieshouse.api.ApiClient;

/**
 * The {@code ApiClientService} interface provides an abstraction that can be
 * used when testing {@code ApiClientManager} static methods, without imposing
 * the use of a test framework that supports mocking of static methods.
 */
public interface ApiClientService {

    ApiClient getApiClient();
}
