package uk.gov.companieshouse.transactions.web.model.presenter;

import java.util.List;

/**
 * Represents the response from the (mock) Presenter API.
 */
public class PresenterApiResponse {

    private IndividualUserDto individualUser;
    private List<String> statements;

    public PresenterApiResponse() {}

    public PresenterApiResponse(IndividualUserDto individualUser, List<String> statements) {
        this.individualUser = individualUser;
        this.statements = statements;
    }

    public IndividualUserDto getIndividualUser() {
        return individualUser;
    }

    public void setIndividualUser(IndividualUserDto individualUser) {
        this.individualUser = individualUser;
    }

    public List<String> getStatements() {
        return statements;
    }

    public void setStatements(List<String> statements) {
        this.statements = statements;
    }
}
