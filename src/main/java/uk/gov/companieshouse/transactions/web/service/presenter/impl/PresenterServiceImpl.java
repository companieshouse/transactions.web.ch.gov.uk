package uk.gov.companieshouse.transactions.web.service.presenter.impl;

import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.presenters.api.PresenterDetailsRequestApi;
import uk.gov.companieshouse.api.presenters.api.PresenterTypeRequestApi;
import uk.gov.companieshouse.api.presenters.model.Acsp;
import uk.gov.companieshouse.api.presenters.model.Individual;
import uk.gov.companieshouse.api.presenters.model.PresenterDetails;
import uk.gov.companieshouse.api.presenters.model.PresenterTypes;
import uk.gov.companieshouse.transactions.web.exception.AcspNotLinkedException;
import uk.gov.companieshouse.transactions.web.exception.UnverifiedException;
import uk.gov.companieshouse.transactions.web.model.presenter.IndividualUserDto;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterApiResponse;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterType;
import uk.gov.companieshouse.transactions.web.service.presenter.PresenterService;

import java.util.List;

public class PresenterServiceImpl implements PresenterService {

    PresenterDetailsRequestApi detailsApi;
    PresenterTypeRequestApi typeApi;

    public PresenterServiceImpl(PresenterDetailsRequestApi detailsApi, PresenterTypeRequestApi typeApi) {
        this.detailsApi = detailsApi;
        this.typeApi = typeApi;
    }

    @Override
    public List<String> getPresenterTypes(String chsUserId, String formType) {
        ResponseEntity<PresenterTypes> presenterTypes = typeApi.getPresenterTypes(chsUserId, formType);
        return presenterTypes.getBody().getTypes();
    }

    @Override
    public PresenterApiResponse getStatements(String transactionId, String companyNumber, String formType, String chsUserId, PresenterType presenterType) throws AcspNotLinkedException, UnverifiedException {
        ResponseEntity<PresenterDetails> presenterDetails = detailsApi.getPresenterDetails(chsUserId, formType, presenterType.toString());
        Acsp acsp = presenterDetails.getBody().getAcsp();

        if (acsp == null) {
            throw new AcspNotLinkedException("Acsp data not found");
        }

        List<String> statements = presenterDetails.getBody().getStatements();
        Individual individual = presenterDetails.getBody().getIndividual();

        IndividualUserDto individualUserDto = new IndividualUserDto();
        individualUserDto.setDob(individual.getDateOfBirth().toString());
        individualUserDto.setName(individual.getName().toString());
        individualUserDto.setUvid(individual.getVerificationId());

        PresenterApiResponse response = new PresenterApiResponse();
        response.setStatements(statements);
        response.setIndividualUser(individualUserDto);
        return response;
    }
}
