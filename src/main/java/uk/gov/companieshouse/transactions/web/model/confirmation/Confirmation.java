package uk.gov.companieshouse.transactions.web.model.confirmation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Confirmation {

    private String companyName;

    private String companyNumber;

    private String confirmationDescription;

    private String closedAtDate;

    private String closedAtTime;

    private String transactionId;

    private String closedBy;
}
