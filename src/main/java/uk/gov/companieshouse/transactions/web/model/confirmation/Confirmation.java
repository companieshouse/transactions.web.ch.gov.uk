package uk.gov.companieshouse.transactions.web.model.confirmation;

public class Confirmation {

    private String companyName;

    private String companyNumber;

    private String confirmationDescription;

    private String closedAtDate;

    private String closedAtTime;

    private String transactionId;

    private String closedBy;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getConfirmationDescription() {
        return confirmationDescription;
    }

    public void setConfirmationDescription(String confirmationDescription) {
        this.confirmationDescription = confirmationDescription;
    }

    public String getClosedAtDate() {
        return closedAtDate;
    }

    public void setClosedAtDate(String closedAtDate) {
        this.closedAtDate = closedAtDate;
    }

    public String getClosedAtTime() {
        return closedAtTime;
    }

    public void setClosedAtTime(String closedAtTime) {
        this.closedAtTime = closedAtTime;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }
}
