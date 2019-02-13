package com.pp.subscription.core;

import com.mongodb.DBObject;
import com.pp.database.dao.subscription.SchemaSubscriptionIndividualsDAO;
import com.pp.database.model.semantic.schema.PropertyDefinition;
import com.pp.database.model.subscription.SchemaSubscription;
import com.pp.database.model.subscription.SchemaSubscriptionIndividuals;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class SubscriptionScanner {

	@Autowired
	private SchemaSubscriptionIndividualsDAO schemaSubscriptionIndividualsDAO;

	public List<SchemaSubscriptionIndividuals> analyseSubscriptions(List<SchemaSubscription> subscriptions,List<DBObject> population) {
		log.info("Analysing subscriptions : {} subscriptions on {} individuals ",subscriptions.size(),population.size());
		List<SchemaSubscriptionIndividuals> updatedSubscriptions = new ArrayList<>();
		for(SchemaSubscription subscription : subscriptions) {
			SchemaSubscriptionIndividuals schemaSubscriptionIndividuals = this.schemaSubscriptionIndividualsDAO.getBySubscriptionId(subscription.getStringId());
			for(DBObject individual : population) {
				boolean doMatch = this.doMatch(subscription, individual);
				if(doMatch) {
					log.info("Matched subscription vs Individual : {} {}",subscription,individual);
					schemaSubscriptionIndividuals.addMatchedIndividual(individual.get("_id").toString());
					updatedSubscriptions.add(schemaSubscriptionIndividuals);
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
