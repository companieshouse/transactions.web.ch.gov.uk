package uk.gov.companieshouse.transactions.web.model.whatisyourrole;

public class Role {

    private final String id;

    private final String description;

    public Role(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}
