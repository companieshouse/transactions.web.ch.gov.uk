package uk.gov.companieshouse.transactions.web.service.presentertype.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.presentertype.PresenterType;
import uk.gov.companieshouse.transactions.web.service.presentertype.PresenterTypesService;

/**
 * Returns hard-coded presenter types until the presenter type API is available. Replace with an
 * API-backed implementation of {@link PresenterTypesService} and delete this class.
 */
@Service
public class MockPresenterTypesServiceImpl implements PresenterTypesService {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PresenterType> getPresenterTypes(String chsUserId, String formType) throws ServiceException {

        return List.of(
                new PresenterType("officer-employee", "Officer employee"),
                new PresenterType("corporate-officer-employee", "Corporate officer employee"),
                new PresenterType("judicial-factor", "A judicial factor"),
                new PresenterType("official-receiver", "Official receiver"),
                new PresenterType("individual-filing-for-self", "Individual filing for yourself"),
                new PresenterType("individual-filing-for-someone-else", "Individual filing for someone else"),
                new PresenterType("ascp", "ASCP sole trader"));
    }
}
