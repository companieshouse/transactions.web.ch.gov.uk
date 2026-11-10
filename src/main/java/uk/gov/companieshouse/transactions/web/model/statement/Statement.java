package uk.gov.companieshouse.transactions.web.model.statement;

public class Statement {
    private String id;
    private String statementText;

    public Statement(String id, String statementText) {
        this.id = id;
        this.statementText = statementText;
    }

    public String getId() {
        return id;
    }

    public String getstatement() {
        return statementText;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setstatement(String statement) {
        this.statementText = statement;
    }
}
