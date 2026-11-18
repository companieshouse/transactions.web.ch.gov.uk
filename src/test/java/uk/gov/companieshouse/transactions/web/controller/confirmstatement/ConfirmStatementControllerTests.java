package uk.gov.companieshouse.transactions.web.controller.confirmstatement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.companieshouse.transactions.web.utils.constants.TokenClaims.RETURN_URL;
import static uk.gov.companieshouse.transactions.web.utils.constants.UrlConstants.CONFIRM_STATEMENTS_PATH;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.nimbusds.jwt.JWTClaimsSet;

import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionWithProtectedData;
import uk.gov.companieshouse.transactions.web.controller.confirmstatements.ConfirmStatementsController;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.statement.Statement;
import uk.gov.companieshouse.transactions.web.service.confirmstatements.ConfirmStatementsService;
import uk.gov.companieshouse.transactions.web.service.transaction.TransactionsService;
import uk.gov.companieshouse.transactions.web.service.token.TokenService;
import uk.gov.companieshouse.transactions.web.session.SessionService;

@ExtendWith(MockitoExtension.class)
class ConfirmStatementControllerTests {

    private static final String USER_ID = "0010";

    private static final String FORM_TYPE = "AA01";

    private static final String TRANSACTION_ID = "123456-654321-123456";

    private static final String TOKEN = "transactions-web.token";

    private MockMvc mockMvc;

    @Mock
    private ConfirmStatementsService confirmStatementsService;

    @Mock
    private SessionService sessionService;

    @Mock
    private TokenService tokenService;

    @Mock
    private TransactionsService transactionsService;

    @InjectMocks
    private ConfirmStatementsController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET returns the page with a list of statements from the last page")
    void getConfirmStatement() throws Exception {
        List<Statement> mockedStatements = List.of(new Statement("id", "statementText"));
        when(confirmStatementsService.getConfirmStatements(USER_ID)).thenReturn(mockedStatements);
        givenValidToken();
        givenSessionUserId(USER_ID);

        mockMvc.perform(get(
                CONFIRM_STATEMENTS_PATH,
                TRANSACTION_ID).param("token", TOKEN))
                .andExpect(status().isOk()).andExpect(view().name("confirmStatement"))
                .andExpect(model().attribute("token", TOKEN))
                .andExpect(model().attribute("formAction",
                        CONFIRM_STATEMENTS_PATH.replace("{transactionId}", TRANSACTION_ID)))
                .andExpect(model().attribute("templateName", "confirmStatement"));

        verifyNoInteractions(transactionsService);
    }

    @Test
    @DisplayName("GET displays the confirmation page even if selected statements are sent")
    void getConfirmStatementWithSelectedStatements() throws Exception {
        List<Statement> mockedStatements = List.of(new Statement("id", "statementText"));
        when(confirmStatementsService.getConfirmStatements(USER_ID)).thenReturn(mockedStatements);
        givenValidToken();
        givenSessionUserId(USER_ID);

        mockMvc.perform(get(CONFIRM_STATEMENTS_PATH, TRANSACTION_ID).param("token", TOKEN)
                .param("selectedStatements", "statement1", "statement2"))
                .andExpect(status().isOk())
                .andExpect(view().name("confirmStatement"));
    }

    @Test
    @DisplayName("POST redirects after statements are selected")
    void postConfirmStatementAfterFormSubmit() throws Exception {
        givenValidToken();
        givenSessionUserId(USER_ID);

        mockMvc.perform(post(CONFIRM_STATEMENTS_PATH, TRANSACTION_ID).param("token", TOKEN)
                .param("selectedStatements", "statement1", "statement2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:return-url?lang=en&token=transactions-web.token"));

        verify(transactionsService).updateTransaction(eq(TRANSACTION_ID), any(Transaction.class));
        verify(transactionsService).patchTransaction(eq(TRANSACTION_ID), any(TransactionWithProtectedData.class));
    }

    @Test
    @DisplayName("POST returns the error page if the transaction update fails")
    void postConfirmStatementUpdateError() throws Exception {
        givenValidToken();
        givenSessionUserId(USER_ID);
        doThrow(new ServiceException("some error")).when(transactionsService)
                .updateTransaction(eq(TRANSACTION_ID), any(Transaction.class));

        mockMvc.perform(post(CONFIRM_STATEMENTS_PATH, TRANSACTION_ID).param("token", TOKEN)
                .param("selectedStatements", "statement1"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verify(transactionsService, never()).patchTransaction(any(), any());
    }

    @Test
    @DisplayName("POST returns an error page if no statements are selected")
    void postConfirmStatementWithoutSelectedStatements() throws Exception {
        givenValidToken();
        givenSessionUserId(USER_ID);

        mockMvc.perform(post(CONFIRM_STATEMENTS_PATH, TRANSACTION_ID).param("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    @DisplayName("GET returns an error page if statements to confirm can't be retrieved")
    void getConfirmStatementError() throws Exception {
        givenValidToken();
        givenSessionUserId(USER_ID);
        when(confirmStatementsService.getConfirmStatements(USER_ID)).thenThrow(new ServiceException("some error"));
        mockMvc.perform(get(
                CONFIRM_STATEMENTS_PATH, TRANSACTION_ID).param("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    @DisplayName("GET throws a bad request if no token provided")
    void getBadRequest() throws Exception {
        mockMvc.perform(get(
                CONFIRM_STATEMENTS_PATH,
                TRANSACTION_ID)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET without a signed-in session returns the error page")
    void getWithoutSession() throws Exception {
        givenValidToken();
        when(sessionService.getUserId()).thenReturn(null);
        mockMvc.perform(get(
                CONFIRM_STATEMENTS_PATH,
                TRANSACTION_ID).param("token", TOKEN)).andExpect(status().isOk()).andExpect(view().name("error"));

        verifyNoInteractions(confirmStatementsService);
    }

    @Test
    @DisplayName("GET returns the error page if the token fails verification")
    void getTokenInvalidError() throws Exception {
        mockMvc.perform(get(
                CONFIRM_STATEMENTS_PATH,
                TRANSACTION_ID).param("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verifyNoInteractions(confirmStatementsService);
    }

    private void givenValidToken() {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim("form_type", FORM_TYPE)
                .claim("transaction_id", TRANSACTION_ID)
                .claim("role", "id")
                .claim("lang", "en")
                .claim(RETURN_URL, "return-url")
                .build();
        when(tokenService.verifyTransactionsWebToken(TOKEN, TRANSACTION_ID)).thenReturn(claims);
    }

    private void givenSessionUserId(String userId) {
        when(sessionService.getUserId()).thenReturn(userId);
    }
}
