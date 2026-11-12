package uk.gov.companieshouse.transactions.web.model.statement;

public class Statement {
    private final String id;
    private final String statementText;

    public Statement(String id, String statementText) {
        this.id = id;
        this.statementText = statementText;
    }

    public String getId() {
        return id;
    }

    public String getStatementText() {
        return statementText;
    }
}
