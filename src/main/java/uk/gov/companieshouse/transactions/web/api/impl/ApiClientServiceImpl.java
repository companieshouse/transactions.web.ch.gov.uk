package uk.gov.companieshouse.transactions.web.api.impl;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.sdk.manager.ApiClientManager;
import uk.gov.companieshouse.transactions.web.api.ApiClientService;

@Component
public class ApiClientServiceImpl implements ApiClientService {

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiClient getApiClient() {
        return ApiClientManager.getSDK();
    }
}
