package uk.gov.companieshouse.transactions.web.service.presentertype;

import java.util.List;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.presentertype.PresenterType;

public interface PresenterTypesService {

    /**
     * Retrieves the presenter types available to a user for a form type
     * @param chsUserId The id of the CHS user
     * @param formType The type of form being filed, e.g. AD01
     * @return A list of {@link PresenterType}
     * @throws ServiceException if the presenter types cannot be retrieved
     */
    List<PresenterType> getPresenterTypes(String chsUserId, String formType) throws ServiceException;
}
