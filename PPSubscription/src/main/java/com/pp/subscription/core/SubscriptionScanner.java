package com.pp.subscription.core;

import com.mongodb.DBObject;
import com.pp.database.model.semantic.schema.PropertyDefinition;
import com.pp.database.model.subscription.SchemaSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SubscriptionScanner {

	private static final Logger log = LoggerFactory.getLogger(SubscriptionScanner.class);
	
	
	public Set<SchemaSubscription> analyseSubscriptions(List<SchemaSubscription> subscriptions,List<DBObject> population) {
		log.info("Analysing subscriptions : {} subscriptions on {} individuals ",subscriptions.size(),population.size());
		Set<SchemaSubscription> updatedSubscriptions = new HashSet<>();
		for(SchemaSubscription subscription : subscriptions) {
			for(DBObject individual : population) {
				boolean doMatch = this.doMatch(subscription, individual);
				if(doMatch) {
					log.info("Matched subscription vs Individual : {} {}",subscription,individual);
					subscription.addMatchedIndividual(individual.get("_id").toString());
					updatedSubscriptions.add(subscription);
				}
			}
		}
		return updatedSubscriptions;
	}
	
	
	public boolean doMatch(SchemaSubscription subscription,DBObject individual) {
		boolean doMatch = false;

		if(individual.get("schemaName").toString().equals(subscription.getSchema().getName())) {
			for(PropertyDefinition property :  subscription.getSchema().getAllProperties()) {
				// Process only filtered fields
				DBObject subscriptionFilter = subscription.getSubscriptionFilter();
				if(subscriptionFilter.containsField(property.getName())) {
					Object filterField = ((DBObject)subscriptionFilter.get(property.getName())).get("uniqueProperty");
					Object individualField = individual.get(property.getName());
					if(filterField.equals(individualField)) {
						doMatch = true;
					}else {
						break;
					}
				}else{
					doMatch = true;
				}
			};
		}
		return doMatch;
	}
}
