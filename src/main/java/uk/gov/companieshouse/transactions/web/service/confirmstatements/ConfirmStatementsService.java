package uk.gov.companieshouse.transactions.web.service.confirmstatements;

import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.transactions.web.model.statements.Statements;

public interface ConfirmStatementsService {
    Statements getConfirmStatements(String chsUserID) throws ServiceException;
}
