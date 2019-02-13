package com.pp.database.model.subscription;

import com.pp.database.kernel.PPEntity;
import com.pp.database.model.subscription.SchemaSubscription;
import jdk.nashorn.internal.ir.annotations.Reference;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SchemaSubscriptionIndividuals extends PPEntity {


    @Reference
    private SchemaSubscription schemaSubscription;
    private List<String> matchedIndividualsIds = new ArrayList<>();


    public void addMatchedIndividual(String individualId) {
        this.matchedIndividualsIds.add(individualId);
    }
}
