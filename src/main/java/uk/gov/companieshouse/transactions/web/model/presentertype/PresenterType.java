package uk.gov.companieshouse.transactions.web.model.presentertype;

/**
 * A presenter type as returned by the presenter type API. The fields are provisional
 * until the API contract is known.
 */
public class PresenterType {

    private final String id;

    private final String description;

    public PresenterType(String id, String description) {
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
