package uk.gov.companieshouse.transactions.web.model.statements;

import java.util.List;

public class Statements {
    List<Statement> statementList;

    public Statements(List<Statement> statements) {
        this.statementList = statements;
    }

    public void setStatementList(List<Statement> statements) {
        this.statementList = statements;
    }

    public List<Statement> getStatementList() {
        return statementList;
    }
}
