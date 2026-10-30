package uk.gov.companieshouse.transactions.web.service.presenter;

import uk.gov.companieshouse.transactions.web.exception.AcspNotLinkedException;
import uk.gov.companieshouse.transactions.web.exception.UnverifiedException;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterApiResponse;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterType;

import java.util.List;

public interface PresenterService {

    List<String> getPresenterTypes(String chsUserId, String formType) throws Exception;

    PresenterApiResponse getStatements(String transactionId, String companyNumber, String formType,
                                       String chsUserId, PresenterType presenterType) throws AcspNotLinkedException, UnverifiedException;
}
