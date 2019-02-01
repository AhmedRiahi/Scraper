package com.pp.subscription.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.pp.database.dao.semantic.PPIndividualSchemaDAO;
import com.pp.database.dao.subscription.SchemaSubscriptionDAO;
import com.pp.database.kernel.MongoDatastore;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.database.model.semantic.schema.IndividualSchema;
import com.pp.database.model.subscription.SchemaSubscription;
import com.pp.subscription.core.SubscriptionScanner;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class SubscriptionService {
	
	private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

	@Autowired
	private SchemaSubscriptionDAO schemaSubscriptionDAO;
	@Autowired
	private PPIndividualSchemaDAO individualSchemaDAO;
	@Autowired
	private SubscriptionScanner subscriptionScanner;

	
	public void createSubscription(SchemaSubscription schemaSubscription) {
		this.schemaSubscriptionDAO.save(schemaSubscription);
	}
	
	public List<SchemaSubscription> getAllSubscriptions(){
		return this.schemaSubscriptionDAO.find().asList();
	}
	
	
	public void scanDescriptorPopulation(DescriptorWorkflowDataPackage dwdp) {
		log.info("Scanning descriptor popululation subscriptions {}",dwdp.getDescriptorJob().getDescriptor().getStringId());
		Set<String> schemasNames = dwdp.getSchemasNames();
		schemasNames.stream().forEach(schemaName -> {
			IndividualSchema schema = this.individualSchemaDAO.findByName(schemaName);
			List<SchemaSubscription> subscriptions = this.schemaSubscriptionDAO.getSubscriptionsBySchemaName(schema.getStringId());
			DBCollection collection = MongoDatastore.getPublishDatastore().getDB().getCollection(schemaName);
			DBObject query = new BasicDBObject();
			query.put("workflowId", dwdp.getStringId());
			List<DBObject> population = collection.find(query).toArray();
			Set<SchemaSubscription> updatedSubscriptions = this.subscriptionScanner.analyseSubscriptions(subscriptions, population);
			
			//Update subscription on database
			for(SchemaSubscription subscription : updatedSubscriptions) {
				this.schemaSubscriptionDAO.save(subscription);
			}
			
		});
	}
	
	
	public List<DBObject> getSubscriptionIndividualsByCount(String subscriptionId,int count){
		SchemaSubscription subscription = this.schemaSubscriptionDAO.get(subscriptionId);
		List<String> matchedIndividualsIds = subscription.getMatchedIndividualsIds();
		List<DBObject> subscriptionIndividuals = new ArrayList<>();
		DBCollection collection = MongoDatastore.getPublishDatastore().getDB().getCollection(subscription.getSchema().getName());
		int i = 0;
		int j = 0 ;
		while(i < count && j < matchedIndividualsIds.size()) {
			String individualId = matchedIndividualsIds.get(j++);
			DBObject query = new BasicDBObject();
			query.put("_id",new ObjectId(individualId));
			if(collection.find(query).hasNext()) {
				DBObject individual = collection.find(query).next();
				individual.put("id",individualId);
				subscriptionIndividuals.add(individual);
				i++;
			}
		}
		return subscriptionIndividuals;
	}
	
	public List<DBObject> getSubscriptionIndividualsByDate(String subscriptionId,Date date){
		SchemaSubscription subscription = this.schemaSubscriptionDAO.get(subscriptionId);
		List<String> matchedIndividualsIds = subscription.getMatchedIndividualsIds();
		List<DBObject> subscriptionIndividuals = new ArrayList<>();
		DBCollection collection = MongoDatastore.getPublishDatastore().getDB().getCollection(subscription.getSchema().getName());
		int i = 0;
		while(i < matchedIndividualsIds.size()) {
			String individualId = matchedIndividualsIds.get(i++);
			DBObject query = new BasicDBObject();
			query.put("_id", new ObjectId(individualId));
			query.put("creationDate",new BasicDBObject("$gte",date));
			if(collection.find(query).hasNext()) {
				DBObject individual = collection.find(query).next();
				individual.put("id",individualId);
				subscriptionIndividuals.add(individual);
			}
		}
		return subscriptionIndividuals;
	}
}
