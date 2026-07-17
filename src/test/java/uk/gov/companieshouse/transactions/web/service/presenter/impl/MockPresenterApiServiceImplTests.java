package uk.gov.companieshouse.transactions.web.service.presenter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterType;
import uk.gov.companieshouse.transactions.web.service.presenter.PresenterApiService;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MockPresenterApiServiceImplTests {

    private final MockPresenterApiServiceImpl service = new MockPresenterApiServiceImpl();

    private static final String TX_ID = "tx-001";
    private static final String COMPANY = "12345678";
    private static final String FORM_TYPE = "VS01";
    private static final String USER_ID = "user-abc";

    @Test
    @DisplayName("ACSP_OWNER returns Success with non-null response")
    void acspOwnerReturnsSuccess() {
        PresenterApiService.PresenterApiResult result =
                service.call(TX_ID, COMPANY, FORM_TYPE, USER_ID, PresenterType.ACSP_OWNER);
        assertInstanceOf(PresenterApiService.Success.class, result);
        assertNotNull(((PresenterApiService.Success) result).response());
    }

    @Test
    @DisplayName("ACSP_EMPLOYEE returns Success with non-null response")
    void acspEmployeeReturnsSuccess() {
        PresenterApiService.PresenterApiResult result =
                service.call(TX_ID, COMPANY, FORM_TYPE, USER_ID, PresenterType.ACSP_EMPLOYEE);
        assertInstanceOf(PresenterApiService.Success.class, result);
    }

    @Test
    @DisplayName("COMPANY_OFFICER returns Success with non-null response")
    void companyOfficerReturnsSuccess() {
        PresenterApiService.PresenterApiResult result =
                service.call(TX_ID, COMPANY, FORM_TYPE, USER_ID, PresenterType.COMPANY_OFFICER);
        assertInstanceOf(PresenterApiService.Success.class, result);
    }

    @Test
    @DisplayName("EXTERNAL_PRESENTER returns Success with non-null response")
    void externalPresenterReturnsSuccess() {
        PresenterApiService.PresenterApiResult result =
                service.call(TX_ID, COMPANY, FORM_TYPE, USER_ID, PresenterType.EXTERNAL_PRESENTER);
        assertInstanceOf(PresenterApiService.Success.class, result);
    }

    @Test
    @DisplayName("INSOLVENCY_PRACTITIONER returns Success with non-null response")
    void insolvencyPractitionerReturnsSuccess() {
        PresenterApiService.PresenterApiResult result =
                service.call(TX_ID, COMPANY, FORM_TYPE, USER_ID, PresenterType.INSOLVENCY_PRACTITIONER);
        assertInstanceOf(PresenterApiService.Success.class, result);
    }

    @Test
    @DisplayName("Success response has ACSP-specific statements for ACSP types")
    void acspTypeHasAcspStatements() {
        PresenterApiService.Success result =
                (PresenterApiService.Success) service.call(TX_ID, COMPANY, FORM_TYPE, USER_ID, PresenterType.ACSP_OWNER);
        assertNotNull(result.response().getStatements());
        assertNotNull(result.response().getIndividualUser());
        assertNotNull(result.response().getIndividualUser().getUvid());
    }

    @Test
    @DisplayName("Success response has IDV-specific statements for non-ACSP types")
    void nonAcspTypeHasIdvStatements() {
        PresenterApiService.Success result =
                (PresenterApiService.Success) service.call(TX_ID, COMPANY, FORM_TYPE, USER_ID, PresenterType.COMPANY_EMPLOYEE);
        assertNotNull(result.response().getStatements());
        assertNotNull(result.response().getIndividualUser());
    }
}
