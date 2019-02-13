package com.pp.database.dao.subscription;

import com.pp.database.kernel.PPDAO;
import com.pp.database.model.subscription.ClientCheckpoint;
import com.pp.database.model.subscription.SchemaSubscription;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Criteria;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClientCheckpointDAO extends PPDAO<ClientCheckpoint>{

	public ClientCheckpointDAO() {
		super(ClientCheckpoint.class);
	}
	
	
	public ClientCheckpoint getCheckPointByClientIdAndSubscriptionId(String clientId,String subscriptionId){
		Query<ClientCheckpoint> query = this.createQuery().disableValidation().field("schemaSubscription.$id").equal(new ObjectId(subscriptionId));
		query.and(query.criteria("clientId").equal(clientId));
		return query.get();
	}

}
