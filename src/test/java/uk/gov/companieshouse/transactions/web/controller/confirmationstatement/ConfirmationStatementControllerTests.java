package uk.gov.companieshouse.transactions.web.controller.confirmationstatement;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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

import uk.gov.companieshouse.transactions.web.controller.confirmstatements.ConfirmStatementsController;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.statements.Statements;
import uk.gov.companieshouse.transactions.web.model.statements.Statement;
import uk.gov.companieshouse.transactions.web.service.confirmstatements.ConfirmStatementsService;

@ExtendWith(MockitoExtension.class)
public class ConfirmationStatementControllerTests {
    private static final String PATH = "/transaction/confirm";

    private static final String USER_ID = "0010";

    private static final String FORM_TYPE = "AA01";

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
        Statements mockedStatements = new Statements(List.of(new Statement("id", "statementText")));
        when(confirmStatementsService.getConfirmStatements(USER_ID)).thenReturn(mockedStatements);

        mockMvc.perform(get(PATH).param("chs-user-id", USER_ID).param("form-type", FORM_TYPE))
                .andExpect(status().isOk()).andExpect(view().name("confirmStatement"))
                .andExpect(model().attribute("chsUserId", USER_ID)).andExpect(model().attribute("formType", FORM_TYPE))
                .andExpect(model().attribute("nextPagePath", "transaction/next-page"))
                .andExpect(model().attribute("templateName", "confirmStatement"));
    }

    @Test
    @DisplayName("GET returns an error page if statements to confirm can't be retrieved")
    void getConfirmStatementError() throws Exception {
        when(confirmStatementsService.getConfirmStatements(USER_ID)).thenThrow(new ServiceException("some error"));
        mockMvc.perform(get(PATH).param("chs-user-id", USER_ID).param("form-type", FORM_TYPE))
                .andExpect(status().isOk()).andExpect(view().name("error"));
    }

    @Test
    @DisplayName("GET throws a bad request if no params listed")
    void getBadRequest() throws Exception {
        mockMvc.perform(get(PATH)).andExpect(status().isBadRequest());
    }

}
