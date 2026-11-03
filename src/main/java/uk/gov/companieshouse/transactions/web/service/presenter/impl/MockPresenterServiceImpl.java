package uk.gov.companieshouse.transactions.web.service.presenter.impl;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.transactions.web.exception.AcspNotLinkedException;
import uk.gov.companieshouse.transactions.web.exception.UnverifiedException;
import uk.gov.companieshouse.transactions.web.model.presenter.IndividualUserDto;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterApiResponse;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterType;
import uk.gov.companieshouse.transactions.web.service.presenter.PresenterService;

import java.util.List;

@Service
public class MockPresenterServiceImpl implements PresenterService {
    @Override
    public List<String> getPresenterTypes(String chsUserId, String formType) throws Exception {
        return List.of();
    }

    @Override
    public PresenterApiResponse getStatements(String transactionId, String companyNumber, String formType, String chsUserId, PresenterType presenterType) throws AcspNotLinkedException, UnverifiedException {
        IndividualUserDto individual = new IndividualUserDto();
        PresenterApiResponse response = new PresenterApiResponse();
        response.setIndividualUser(individual);
        response.setStatements(List.of());
        return response;
    }
}
