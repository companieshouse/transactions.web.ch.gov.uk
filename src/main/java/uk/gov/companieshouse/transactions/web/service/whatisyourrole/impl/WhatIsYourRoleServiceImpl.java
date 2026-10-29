package uk.gov.companieshouse.transactions.web.service.whatisyourrole.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.transactions.web.model.presentertype.PresenterType;
import uk.gov.companieshouse.transactions.web.model.whatisyourrole.WhatIsYourRole;
import uk.gov.companieshouse.transactions.web.service.whatisyourrole.WhatIsYourRoleService;
import uk.gov.companieshouse.transactions.web.transformer.whatisyourrole.WhatIsYourRoleTransformer;

@Service
public class WhatIsYourRoleServiceImpl implements WhatIsYourRoleService {

    private final WhatIsYourRoleTransformer whatIsYourRoleTransformer;

    public WhatIsYourRoleServiceImpl(WhatIsYourRoleTransformer whatIsYourRoleTransformer) {
        this.whatIsYourRoleTransformer = whatIsYourRoleTransformer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WhatIsYourRole getWhatIsYourRole(List<PresenterType> presenterTypes) {

        return whatIsYourRoleTransformer.getWhatIsYourRole(presenterTypes);
    }
}
