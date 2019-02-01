package com.pp.database.dao.subscription;

import com.pp.database.kernel.PPDAO;
import com.pp.database.model.subscription.SchemaSubscription;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SchemaSubscriptionDAO extends PPDAO<SchemaSubscription>{

	public SchemaSubscriptionDAO() {
		super(SchemaSubscription.class);
	}
	
	
	public List<SchemaSubscription> getSubscriptionsBySchemaName(String schemaId){
		return this.createQuery().disableValidation().field("schema.$id").equal(new ObjectId(schemaId)).asList();
	}

}
