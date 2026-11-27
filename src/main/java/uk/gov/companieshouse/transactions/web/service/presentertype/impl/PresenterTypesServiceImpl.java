package uk.gov.companieshouse.transactions.web.service.presentertype.impl;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.presenters.model.PresenterTypes;
import uk.gov.companieshouse.transactions.web.api.ApiClientService;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.presentertype.PresenterType;
import uk.gov.companieshouse.transactions.web.service.presentertype.PresenterTypesService;

@Service
public class PresenterTypesServiceImpl implements PresenterTypesService {

    // The API returns ids only, so the text shown to the user is looked up here
    private static final Map<String, String> DESCRIPTIONS = Map.of(
            "officer-employee", "Officer employee",
            "corporate-officer-employee", "Corporate officer employee",
            "judicial-factor", "A judicial factor",
            "official-receiver", "Official receiver",
            "individual-filing-for-self", "Individual filing for yourself",
            "individual-filing-for-someone-else", "Individual filing for someone else",
            "ascp", "ASCP sole trader");

    private final ApiClientService apiClientService;

    public PresenterTypesServiceImpl(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PresenterType> getPresenterTypes(String chsUserId, String formType) throws ServiceException {

        try {
            PresenterTypes presenterTypes = apiClientService.getInternalApiClient()
                    .privatePresentersResourceHandler()
                    .getPresenterTypes(formType)
                    .execute()
                    .getData();

            if (presenterTypes == null || presenterTypes.getTypes() == null) {
                throw new ServiceException("No presenter types returned for form type " + formType);
            }

            return presenterTypes.getTypes().stream()
                    .map(type -> new PresenterType(type, DESCRIPTIONS.getOrDefault(type, type)))
                    .toList();

        } catch (ApiErrorResponseException e) {
            throw new ServiceException("Error retrieving presenter types", e);
        } catch (URIValidationException e) {
            throw new ServiceException("Invalid URI for presenter types resource", e);
        }
    }
}
