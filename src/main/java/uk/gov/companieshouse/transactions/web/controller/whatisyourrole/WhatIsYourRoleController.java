package uk.gov.companieshouse.transactions.web.controller.whatisyourrole;

import java.util.HashMap;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.transactions.web.TransactionsWebApplication;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.presentertype.PresenterType;
import uk.gov.companieshouse.transactions.web.service.presentertype.PresenterTypesService;
import uk.gov.companieshouse.transactions.web.service.whatisyourrole.WhatIsYourRoleService;

@Controller
@RequestMapping("/transaction/what-is-your-role")
public class WhatIsYourRoleController {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TransactionsWebApplication.APPLICATION_NAME_SPACE);

    private static final String ERROR_PAGE = "error";

    private static final String TEMPLATE_NAME = "whatIsYourRole";

    // Placeholder until the path of the next page in the journey is known
    private static final String NEXT_PAGE_PATH = "/transaction/next-page";

    private final PresenterTypesService presenterTypesService;
    private final WhatIsYourRoleService whatIsYourRoleService;

    public WhatIsYourRoleController(PresenterTypesService presenterTypesService,
                                    WhatIsYourRoleService whatIsYourRoleService) {
        this.presenterTypesService = presenterTypesService;
        this.whatIsYourRoleService = whatIsYourRoleService;
    }

    @GetMapping
    public String getWhatIsYourRole(@RequestParam("chs-user-id") String chsUserId,
                                    @RequestParam("form-type") String formType,
                                    HttpServletRequest request,
                                    Model model) {

        LOGGER.infoRequest(request, "What is your role request received for form type " + formType,
                new HashMap<>());

        try {
            List<PresenterType> presenterTypes = presenterTypesService.getPresenterTypes(chsUserId, formType);

            model.addAttribute("whatIsYourRole", whatIsYourRoleService.getWhatIsYourRole(presenterTypes));
            model.addAttribute("chsUserId", chsUserId);
            model.addAttribute("formType", formType);
            model.addAttribute("nextPagePath", NEXT_PAGE_PATH);

            return getTemplateName();

        } catch (ServiceException ex) {

            LOGGER.errorRequest(request, ex.getMessage(), ex);
            return ERROR_PAGE;
        }
    }

    @ModelAttribute("templateName")
    private static String getTemplateName() {

        return TEMPLATE_NAME;
    }
}
