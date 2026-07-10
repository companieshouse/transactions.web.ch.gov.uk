package uk.gov.companieshouse.transactions.web.model.presenter;

public enum PresenterType {

    ACSP_OWNER("ACSP owner"),
    ACSP_EMPLOYEE("Employee of an ACSP"),
    COMPANY_OFFICER("Officer of the company"),
    COMPANY_EMPLOYEE("Employee of the company"),
    CORPORATE_DIRECTOR_EMPLOYEE("Employee of a corporate director"),
    EXTERNAL_PRESENTER("External presenter (identity verified individual)"),
    INSOLVENCY_PRACTITIONER("Insolvency practitioner"),
    NONE_OF_THE_ABOVE("None of the above");

    private final String displayName;

    PresenterType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
