package uk.gov.companieshouse.transactions.web.model.statements;

public class Statement {
    public String id;
    public String statementText;

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
