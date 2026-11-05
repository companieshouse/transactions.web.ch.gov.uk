package uk.gov.companieshouse.transactions.web.controller.whatisyourrole;

import static uk.gov.companieshouse.transactions.web.controller.whatisyourrole.WhatIsYourRoleConstants.NEXT_PAGE_PATH;
import static uk.gov.companieshouse.transactions.web.controller.whatisyourrole.WhatIsYourRoleConstants.WHAT_IS_YOUR_ROLE_PATH;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.util.DataMap;
import uk.gov.companieshouse.transactions.web.TransactionsWebApplication;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.presentertype.PresenterType;
import uk.gov.companieshouse.transactions.web.model.whatisyourrole.Role;
import uk.gov.companieshouse.transactions.web.model.whatisyourrole.WhatIsYourRole;
import uk.gov.companieshouse.transactions.web.service.presentertype.PresenterTypesService;

@Controller
@RequestMapping(WHAT_IS_YOUR_ROLE_PATH)
public class WhatIsYourRoleController {

    private static final Logger LOG =
            LoggerFactory.getLogger(TransactionsWebApplication.APPLICATION_NAME_SPACE);

    private static final String ERROR_PAGE = "error";

    private static final String TEMPLATE_NAME = "whatIsYourRole";

    private final PresenterTypesService presenterTypesService;

    public WhatIsYourRoleController(PresenterTypesService presenterTypesService) {
        this.presenterTypesService = presenterTypesService;
    }

    @GetMapping
    public String getWhatIsYourRole(@RequestParam("chs-user-id") String chsUserId,
                                    @RequestParam("form-type") String formType,
                                    @RequestHeader(value = "X-Request-Id", required = false) String xRequestId,
                                    Model model) {

        DataMap whatIsYourRoleDataMap = new DataMap.Builder().chsUserId(chsUserId).build();

        LOG.infoContext(xRequestId, "What is your role request received for form type " + formType, whatIsYourRoleDataMap.getLogMap());

        try {
            List<PresenterType> presenterTypes = presenterTypesService.getPresenterTypes(chsUserId, formType);

            model.addAttribute("whatIsYourRole", toWhatIsYourRole(presenterTypes));
            model.addAttribute("chsUserId", chsUserId);
            model.addAttribute("formType", formType);
            model.addAttribute("nextPagePath", NEXT_PAGE_PATH);

            return getTemplateName();

        } catch (ServiceException ex) {

            LOG.errorContext(xRequestId, ex.getMessage(), ex, whatIsYourRoleDataMap.getLogMap());
            return ERROR_PAGE;
        }
    }

    private WhatIsYourRole toWhatIsYourRole(List<PresenterType> presenterTypes) {
        WhatIsYourRole result = new WhatIsYourRole();
        result.setRoles(presenterTypes.stream()
                .map(type -> new Role(type.getId(), type.getDescription()))
                .toList());
        return result;
    }

    @ModelAttribute("templateName")
    private static String getTemplateName() {

        return TEMPLATE_NAME;
    }
}
