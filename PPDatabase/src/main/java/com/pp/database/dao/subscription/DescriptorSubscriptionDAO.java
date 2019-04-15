package com.pp.database.dao.subscription;

import com.pp.database.kernel.PPDAO;
import com.pp.database.model.subscription.DescriptorSubscription;
import com.pp.database.model.subscription.SchemaSubscription;
import org.springframework.stereotype.Repository;

@Repository
public class DescriptorSubscriptionDAO extends PPDAO<DescriptorSubscription> {

    public DescriptorSubscriptionDAO() {
        super(DescriptorSubscription.class);
    }

}
