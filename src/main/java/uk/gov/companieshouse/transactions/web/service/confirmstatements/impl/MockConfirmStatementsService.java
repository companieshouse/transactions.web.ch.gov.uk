package uk.gov.companieshouse.transactions.web.service.confirmstatements.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.transactions.web.model.statements.Statement;
import uk.gov.companieshouse.transactions.web.model.statements.Statements;
import uk.gov.companieshouse.transactions.web.service.confirmstatements.ConfirmStatementsService;

@Service
public class MockConfirmStatementsService implements ConfirmStatementsService {

    public Statements getConfirmStatements(String chsUserId) throws ServiceException {
        return new Statements(
                List.of(new Statement("delivery-on-behalf-of-firm", "I am delivering on behalf of a firm"),
                        new Statement("officer-employee-statement", "I am an officer or employee of the firm"),
                        new Statement("verified-statement", "My identity is verified")));
    }
}
