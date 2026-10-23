package uk.gov.companieshouse.transactions.web.transformer.whatisyourrole;

import java.util.List;
import uk.gov.companieshouse.transactions.web.model.presentertype.PresenterType;
import uk.gov.companieshouse.transactions.web.model.whatisyourrole.WhatIsYourRole;

public interface WhatIsYourRoleTransformer {

    /**
     * Get the {@link WhatIsYourRole} model for the provided presenter types
     * @param presenterTypes The presenter types to offer as roles
     * @return A {@link WhatIsYourRole} model
     */
    WhatIsYourRole getWhatIsYourRole(List<PresenterType> presenterTypes);
}
