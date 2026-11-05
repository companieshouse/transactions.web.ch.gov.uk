package uk.gov.companieshouse.transactions.web.service.confirmstatements;

import java.util.List;

import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.statement.Statement;

public interface ConfirmStatementsService {
    List<Statement> getConfirmStatements(String chsUserID) throws ServiceException;
}
