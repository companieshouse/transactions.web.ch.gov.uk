package uk.gov.companieshouse.transactions.web.controller.presenter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.presenter.IndividualUserDto;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterApiError;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterApiResponse;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterType;
import uk.gov.companieshouse.transactions.web.service.presenter.PresenterApiService;
import uk.gov.companieshouse.transactions.web.service.transaction.TransactionsService;
import uk.gov.companieshouse.transactions.web.session.SessionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
public class PresenterControllerTests {

    private MockMvc mockMvc;

    @Mock
    private TransactionsService transactionsService;

    @Mock
    private PresenterApiService presenterApiService;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private PresenterController controller;

    private static final String TRANSACTION_ID = "abc-123-def";
    private static final String RETURN_URL = "/psc-verification/presenter-return/" + TRANSACTION_ID;
    private static final String ACSP_SIGN_IN_URL = "http://acsp.chs.local/sign-in";
    private static final String IDV_URL = "http://idv.chs.local/verify";

    private Map<String, Object> sessionData;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        ReflectionTestUtils.setField(controller, "acspSignInUrl", ACSP_SIGN_IN_URL);
        ReflectionTestUtils.setField(controller, "identityVerificationUrl", IDV_URL);
        ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());

        sessionData = new HashMap<>();
        lenient().when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);
    }

    // -----------------------------------------------------------------------
    // GET /transaction/{id}/presenter
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /presenter - renders presenter type page and stores returnUrl in session")
    void getPresenterTypeStoresReturnUrlAndRendersPage() throws Exception {
        mockMvc.perform(get("/transaction/{id}/presenter", TRANSACTION_ID)
                        .param("returnUrl", RETURN_URL))
                .andExpect(status().isOk())
                .andExpect(view().name("presenter/presenterType"))
                .andExpect(model().attributeExists("presenterTypes"));

        assert sessionData.get(PresenterController.SESSION_RETURN_URL).equals(RETURN_URL);
    }

    @Test
    @DisplayName("GET /presenter - renders page without error when no returnUrl supplied")
    void getPresenterTypeNoReturnUrl() throws Exception {
        mockMvc.perform(get("/transaction/{id}/presenter", TRANSACTION_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("presenter/presenterType"));
    }

    // -----------------------------------------------------------------------
    // POST /transaction/{id}/presenter — routing
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /presenter with NONE_OF_THE_ABOVE redirects to not-eligible stop screen")
    void postPresenterTypeNoneOfTheAboveRedirectsToStopScreen() throws Exception {
        mockMvc.perform(post("/transaction/{id}/presenter", TRANSACTION_ID)
                        .param("presenterType", "NONE_OF_THE_ABOVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transaction/" + TRANSACTION_ID + "/presenter/stop/not-eligible"));
    }

    @Test
    @DisplayName("POST /presenter with no selection redirects back to type page")
    void postPresenterTypeNoSelectionRedirectsBack() throws Exception {
        mockMvc.perform(post("/transaction/{id}/presenter", TRANSACTION_ID))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transaction/" + TRANSACTION_ID + "/presenter"));
    }

    @Test
    @DisplayName("POST /presenter with valid ACSP type and successful API call redirects to statements")
    void postPresenterTypeAcspSuccessRedirectsToStatements() throws Exception {
        Transaction transaction = buildTransaction();
        when(transactionsService.getTransaction(TRANSACTION_ID)).thenReturn(transaction);
        when(presenterApiService.call(anyString(), anyString(), anyString(), any(), any()))
                .thenReturn(new PresenterApiService.Success(buildApiResponse()));

        mockMvc.perform(post("/transaction/{id}/presenter", TRANSACTION_ID)
                        .param("presenterType", "ACSP_OWNER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transaction/" + TRANSACTION_ID + "/presenter/statements"));
    }

    @Test
    @DisplayName("POST /presenter with ACSP_NOT_LINKED error redirects to acsp-not-linked stop screen")
    void postPresenterTypeAcspNotLinkedRedirectsToStopScreen() throws Exception {
        Transaction transaction = buildTransaction();
        when(transactionsService.getTransaction(TRANSACTION_ID)).thenReturn(transaction);
        when(presenterApiService.call(anyString(), anyString(), anyString(), any(), any()))
                .thenReturn(new PresenterApiService.Failure(PresenterApiError.ACSP_NOT_LINKED));

        mockMvc.perform(post("/transaction/{id}/presenter", TRANSACTION_ID)
                        .param("presenterType", "ACSP_OWNER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transaction/" + TRANSACTION_ID + "/presenter/stop/acsp-not-linked"));
    }

    @Test
    @DisplayName("POST /presenter with IDENTITY_NOT_VERIFIED error redirects to not-verified stop screen")
    void postPresenterTypeIdentityNotVerifiedRedirectsToStopScreen() throws Exception {
        Transaction transaction = buildTransaction();
        when(transactionsService.getTransaction(TRANSACTION_ID)).thenReturn(transaction);
        when(presenterApiService.call(anyString(), anyString(), anyString(), any(), any()))
                .thenReturn(new PresenterApiService.Failure(PresenterApiError.IDENTITY_NOT_VERIFIED));

        mockMvc.perform(post("/transaction/{id}/presenter", TRANSACTION_ID)
                        .param("presenterType", "COMPANY_OFFICER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transaction/" + TRANSACTION_ID + "/presenter/stop/not-verified"));
    }

    @Test
    @DisplayName("POST /presenter - service exception when loading transaction returns error page")
    void postPresenterTypeTransactionServiceExceptionReturnsErrorPage() throws Exception {
        when(transactionsService.getTransaction(TRANSACTION_ID))
                .thenThrow(new ServiceException("not found", new RuntimeException()));

        mockMvc.perform(post("/transaction/{id}/presenter", TRANSACTION_ID)
                        .param("presenterType", "COMPANY_OFFICER"))
                .andExpect(view().name("error"));
    }

    // -----------------------------------------------------------------------
    // GET /transaction/{id}/presenter/statements
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /statements - renders statements page when API response in session")
    void getStatementsRendersPageWithApiResponse() throws Exception {
        sessionData.put(PresenterController.SESSION_API_RESPONSE, buildApiResponse());

        mockMvc.perform(get("/transaction/{id}/presenter/statements", TRANSACTION_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("presenter/presenterStatements"))
                .andExpect(model().attributeExists("statements"))
                .andExpect(model().attributeExists("individualUser"));
    }

    @Test
    @DisplayName("GET /statements - returns error page when no API response in session")
    void getStatementsNoApiResponseReturnsErrorPage() throws Exception {
        mockMvc.perform(get("/transaction/{id}/presenter/statements", TRANSACTION_ID))
                .andExpect(view().name("error"));
    }

    // -----------------------------------------------------------------------
    // POST /transaction/{id}/presenter/statements
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /statements - patches data and redirects to returnUrl")
    void postStatementsSuccessRedirectsToReturnUrl() throws Exception {
        sessionData.put(PresenterController.SESSION_API_RESPONSE, buildApiResponse());
        sessionData.put(PresenterController.SESSION_RETURN_URL, RETURN_URL);

        mockMvc.perform(post("/transaction/{id}/presenter/statements", TRANSACTION_ID)
                        .param("selectedStatements", "Statement one")
                        .param("selectedStatements", "Statement two"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(RETURN_URL));

        verify(transactionsService).patchPresenterData(anyString(), any(PresenterApiResponse.class));
    }

    @Test
    @DisplayName("POST /statements - no statements selected redirects back to statements")
    void postStatementsNoSelectionRedirectsBack() throws Exception {
        sessionData.put(PresenterController.SESSION_API_RESPONSE, buildApiResponse());

        mockMvc.perform(post("/transaction/{id}/presenter/statements", TRANSACTION_ID))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/transaction/" + TRANSACTION_ID + "/presenter/statements*"));
    }

    @Test
    @DisplayName("POST /statements - no API response in session returns error page")
    void postStatementsNoApiResponseReturnsErrorPage() throws Exception {
        mockMvc.perform(post("/transaction/{id}/presenter/statements", TRANSACTION_ID)
                        .param("selectedStatements", "Statement"))
                .andExpect(view().name("error"));
    }

    @Test
    @DisplayName("POST /statements - service exception on patch returns error page")
    void postStatementsServiceExceptionReturnsErrorPage() throws Exception {
        sessionData.put(PresenterController.SESSION_API_RESPONSE, buildApiResponse());
        sessionData.put(PresenterController.SESSION_RETURN_URL, RETURN_URL);

        org.mockito.Mockito.doThrow(new ServiceException("patch failed", new RuntimeException()))
                .when(transactionsService).patchPresenterData(anyString(), any());

        mockMvc.perform(post("/transaction/{id}/presenter/statements", TRANSACTION_ID)
                        .param("selectedStatements", "Statement one"))
                .andExpect(view().name("error"));
    }

    // -----------------------------------------------------------------------
    // Stop screens
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /stop/not-eligible renders stop page")
    void stopNotEligibleRendersPage() throws Exception {
        mockMvc.perform(get("/transaction/{id}/presenter/stop/not-eligible", TRANSACTION_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("presenter/stop/notEligible"))
                .andExpect(model().attributeExists("eligibleTypes"));
    }

    @Test
    @DisplayName("GET /stop/acsp-not-linked renders stop page with acspSignInUrl")
    void stopAcspNotLinkedRendersPage() throws Exception {
        mockMvc.perform(get("/transaction/{id}/presenter/stop/acsp-not-linked", TRANSACTION_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("presenter/stop/acspNotLinked"))
                .andExpect(model().attribute("acspSignInUrl", ACSP_SIGN_IN_URL));
    }

    @Test
    @DisplayName("GET /stop/not-verified renders stop page with identityVerificationUrl")
    void stopNotVerifiedRendersPage() throws Exception {
        mockMvc.perform(get("/transaction/{id}/presenter/stop/not-verified", TRANSACTION_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("presenter/stop/notVerified"))
                .andExpect(model().attribute("identityVerificationUrl", IDV_URL));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Transaction buildTransaction() {
        Transaction tx = new Transaction();
        tx.setCompanyNumber("12345678");
        tx.setDescription("PSC Verification Transaction");
        return tx;
    }

    private PresenterApiResponse buildApiResponse() {
        return new PresenterApiResponse(
                new IndividualUserDto("UVID-001", "Test Person", "01/01/1970"),
                List.of("Statement one", "Statement two"));
    }
}
