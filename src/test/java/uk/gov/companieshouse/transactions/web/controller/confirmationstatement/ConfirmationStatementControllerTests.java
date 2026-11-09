package uk.gov.companieshouse.transactions.web.controller.confirmationstatement;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.companieshouse.transactions.web.util.constants.UrlConstants.CONFIRM_STATEMENTS_PATH;

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
import com.nimbusds.jwt.PlainJWT;

import uk.gov.companieshouse.transactions.web.controller.confirmstatements.ConfirmStatementsController;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.statement.Statement;
import uk.gov.companieshouse.transactions.web.service.confirmstatements.ConfirmStatementsService;

@ExtendWith(MockitoExtension.class)
public class ConfirmationStatementControllerTests {

    private static final String USER_ID = "0010";

    private static final String FORM_TYPE = "AA01";

    private static final String TRANSACTION_ID = "123456-654321-123456";
    private static final String TOKEN = new PlainJWT(claims()).serialize();

    private MockMvc mockMvc;

    @Mock
    private ConfirmStatementsService confirmStatementsService;

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

        mockMvc.perform(get(
                CONFIRM_STATEMENTS_PATH,
                TRANSACTION_ID).param("token", TOKEN))
                .andExpect(status().isOk()).andExpect(view().name("confirmStatement"))
                .andExpect(model().attribute("token", TOKEN))
                .andExpect(model().attribute("nextPagePath", "next/page"))
                .andExpect(model().attribute("templateName", "confirmStatement"));

    }

    @Test
    @DisplayName("GET returns an error page if statements to confirm can't be retrieved")
    void getConfirmStatementError() throws Exception {
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
    @DisplayName("GET returns the error page if the JWT is invalid")
    void getJWTInvalidError() throws Exception {
        mockMvc.perform(get(
                CONFIRM_STATEMENTS_PATH,
                TRANSACTION_ID).param("token", "not-a-valid-jwtoken"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    private static JWTClaimsSet claims() {
        return new JWTClaimsSet.Builder().claim("chs-user-id", USER_ID).claim("form-type", FORM_TYPE).build();
    }
}
