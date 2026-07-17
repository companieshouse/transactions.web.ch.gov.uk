package uk.gov.companieshouse.transactions.web.service.presenter.impl;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.transactions.web.model.presenter.IndividualUserDto;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterApiResponse;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterType;
import uk.gov.companieshouse.transactions.web.service.presenter.PresenterApiService;

import java.util.List;

/**
 * Mock implementation of {@link PresenterApiService}.
 *
 * <p>Always returns a successful response with hard-coded identity data and statements.
 * In production this class would be replaced (or extended) to make a real HTTP call
 * to the Presenter API and map the result, returning {@link Failure} when:
 * <ul>
 *   <li>ACSP_OWNER / ACSP_EMPLOYEE — the user's login email is not linked to an ACSP
 *       account ({@code PresenterApiError.ACSP_NOT_LINKED})</li>
 *   <li>All other types — the user has not completed identity verification
 *       ({@code PresenterApiError.IDENTITY_NOT_VERIFIED})</li>
 * </ul>
 */
@Service
public class MockPresenterApiServiceImpl implements PresenterApiService {

    private static final IndividualUserDto MOCK_USER = new IndividualUserDto(
            "11111-222-333-444-55555",
            "Test Person",
            "01/01/1970"
    );

    private static final List<String> MOCK_ACSP_STATEMENTS = List.of(
            "I confirm that I am authorised to file on behalf of this ACSP.",
            "I confirm that the information provided is accurate and complete.",
            "I confirm that I understand my legal obligations as an ACSP representative."
    );

    private static final List<String> MOCK_IDV_STATEMENTS = List.of(
            "I confirm that my identity has been verified.",
            "I confirm that I am authorised to file on behalf of this company.",
            "I confirm that the information provided is accurate and complete."
    );

    @Override
    public PresenterApiResult call(String transactionId, String companyNumber, String formType,
            String chsUserId, PresenterType presenterType) {

        List<String> statements = isAcspType(presenterType) ? MOCK_ACSP_STATEMENTS : MOCK_IDV_STATEMENTS;

        // TODO (production): make HTTP call to Presenter API.
        // For ACSP types, check ACSP linkage by email. Return Failure(ACSP_NOT_LINKED) if not linked.
        // For all other types, check IDV status. Return Failure(IDENTITY_NOT_VERIFIED) if not verified.
        return new Success(new PresenterApiResponse(MOCK_USER, statements));
    }

    private boolean isAcspType(PresenterType presenterType) {
        return presenterType == PresenterType.ACSP_OWNER
                || presenterType == PresenterType.ACSP_EMPLOYEE;
    }
}
