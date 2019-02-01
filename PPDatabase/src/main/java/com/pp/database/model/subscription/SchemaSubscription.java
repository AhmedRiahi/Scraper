package com.pp.database.model.subscription;

import com.mongodb.BasicDBObject;
import com.pp.database.kernel.PPEntity;
import com.pp.database.model.semantic.schema.IndividualSchema;
import org.mongodb.morphia.annotations.Reference;

import java.util.ArrayList;
import java.util.List;

public class SchemaSubscription extends PPEntity{

	@Reference
	private IndividualSchema schema;
	private BasicDBObject subscriptionFilter;
	private List<String> matchedIndividualsIds = new ArrayList<>();


	public void addMatchedIndividual(String individualId) {
		this.matchedIndividualsIds.add(individualId);
	}
	
	
	//---------------------- GETTERS / SETTERS ----------------------
	
	public IndividualSchema getSchema() {
		return schema;
	}
	
	public void setSchema(IndividualSchema schema) {
		this.schema = schema;
	}

	public BasicDBObject getSubscriptionFilter() {
		return subscriptionFilter;
	}

	public void setSubscriptionFilter(BasicDBObject subscriptionFilter) {
		this.subscriptionFilter = subscriptionFilter;
	}
	
	public List<String> getMatchedIndividualsIds() {
		return matchedIndividualsIds;
	}


	public void setMatchedIndividualsIds(List<String> matchedIndividualsIds) {
		this.matchedIndividualsIds = matchedIndividualsIds;
	}

}
