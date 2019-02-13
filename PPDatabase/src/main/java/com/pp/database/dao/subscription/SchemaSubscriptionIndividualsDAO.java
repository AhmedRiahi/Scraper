package com.pp.database.dao.subscription;

import com.pp.database.kernel.PPDAO;
import com.pp.database.model.subscription.SchemaSubscription;
import com.pp.database.model.subscription.SchemaSubscriptionIndividuals;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SchemaSubscriptionIndividualsDAO extends PPDAO<SchemaSubscriptionIndividuals>{

	public SchemaSubscriptionIndividualsDAO() {
		super(SchemaSubscriptionIndividuals.class);
	}


	public SchemaSubscriptionIndividuals getBySubscriptionId(String subscriptionId){
		return this.createQuery().disableValidation().field("schemaSubscription.$id").equal(new ObjectId(subscriptionId)).get();
	}

}
