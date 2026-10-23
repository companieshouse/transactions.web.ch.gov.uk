package uk.gov.companieshouse.transactions.web.controller.whatisyourrole;

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
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.presentertype.PresenterType;
import uk.gov.companieshouse.transactions.web.model.whatisyourrole.WhatIsYourRole;
import uk.gov.companieshouse.transactions.web.service.presentertype.PresenterTypesService;
import uk.gov.companieshouse.transactions.web.service.whatisyourrole.WhatIsYourRoleService;

@ExtendWith(MockitoExtension.class)
class WhatIsYourRoleControllerTests {

    private static final String PATH = "/transaction/what-is-your-role";

    private static final String USER_ID = "1234";

    private static final String FORM_TYPE = "AD01";

    private MockMvc mockMvc;

    @Mock
    private PresenterTypesService presenterTypesService;

    @Mock
    private WhatIsYourRoleService whatIsYourRoleService;

    @InjectMocks
    private WhatIsYourRoleController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET returns the what is your role view with the model")
    void getWhatIsYourRole() throws Exception {

        List<PresenterType> presenterTypes = List.of(new PresenterType("id", "Description"));
        WhatIsYourRole whatIsYourRole = new WhatIsYourRole();
        when(presenterTypesService.getPresenterTypes(USER_ID, FORM_TYPE)).thenReturn(presenterTypes);
        when(whatIsYourRoleService.getWhatIsYourRole(presenterTypes)).thenReturn(whatIsYourRole);

        mockMvc.perform(get(PATH).param("chs-user-id", USER_ID).param("form-type", FORM_TYPE))
                .andExpect(status().isOk())
                .andExpect(view().name("whatIsYourRole"))
                .andExpect(model().attribute("whatIsYourRole", whatIsYourRole))
                .andExpect(model().attribute("chsUserId", USER_ID))
                .andExpect(model().attribute("formType", FORM_TYPE))
                .andExpect(model().attribute("nextPagePath", "/transaction/next-page"))
                .andExpect(model().attribute("templateName", "whatIsYourRole"));
    }

    @Test
    @DisplayName("GET returns the error view if the presenter types cannot be retrieved")
    void getWhatIsYourRoleServiceException() throws Exception {

        when(presenterTypesService.getPresenterTypes(USER_ID, FORM_TYPE))
                .thenThrow(new ServiceException("error"));

        mockMvc.perform(get(PATH).param("chs-user-id", USER_ID).param("form-type", FORM_TYPE))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    @DisplayName("GET without the required params returns bad request")
    void getWithoutParams() throws Exception {

        mockMvc.perform(get(PATH))
                .andExpect(status().isBadRequest());
    }
}
