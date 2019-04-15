package com.pp.subscription.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.util.JSON;
import com.pp.database.dao.semantic.PPIndividualSchemaDAO;
import com.pp.database.dao.subscription.ClientCheckpointDAO;
import com.pp.database.dao.subscription.SchemaSubscriptionDAO;
import com.pp.database.dao.subscription.SchemaSubscriptionIndividualsDAO;
import com.pp.database.kernel.MongoDatastore;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.database.model.semantic.schema.IndividualSchema;
import com.pp.database.model.subscription.ClientCheckpoint;
import com.pp.database.model.subscription.SchemaSubscription;
import com.pp.database.model.subscription.SchemaSubscriptionIndividuals;
import com.pp.subscription.core.IndividualToBeanConverter;
import com.pp.subscription.core.SubscriptionScanner;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SchemaSubscriptionService {

	@Autowired
	private SchemaSubscriptionDAO schemaSubscriptionDAO;
	@Autowired
	private PPIndividualSchemaDAO individualSchemaDAO;
	@Autowired
	private ClientCheckpointDAO clientCheckpointDAO;
	@Autowired
	private SchemaSubscriptionIndividualsDAO schemaSubscriptionIndividualsDAO;
	@Autowired
	private SubscriptionScanner subscriptionScanner;

	@Autowired
	private IndividualToBeanConverter individualToBeanConverter;

	
	public void createSubscription(SchemaSubscription schemaSubscription) {
		this.schemaSubscriptionDAO.save(schemaSubscription);
		this.createClientCheckpoint(schemaSubscription);
		this.createSubscriptionIndividuals(schemaSubscription);
	}

	private void createClientCheckpoint(SchemaSubscription schemaSubscription){
		ClientCheckpoint clientCheckpoint = new ClientCheckpoint();
		String clientId = UUID.randomUUID().toString();
		clientCheckpoint.setClientId(clientId);
		clientCheckpoint.setSchemaSubscription(schemaSubscription);
		this.clientCheckpointDAO.save(clientCheckpoint);
	}

	private void createSubscriptionIndividuals(SchemaSubscription schemaSubscription){
		SchemaSubscriptionIndividuals schemaSubscriptionIndividuals = new SchemaSubscriptionIndividuals();
		schemaSubscriptionIndividuals.setSchemaSubscription(schemaSubscription);
		this.schemaSubscriptionIndividualsDAO.save(schemaSubscriptionIndividuals);
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
			List<SchemaSubscriptionIndividuals> updatedSubscriptions = this.subscriptionScanner.analyseSubscriptions(subscriptions, population);
			
			//Update subscription on database
			for(SchemaSubscriptionIndividuals schemaSubscriptionIndividuals : updatedSubscriptions) {
				this.schemaSubscriptionIndividualsDAO.save(schemaSubscriptionIndividuals);
			}
		});
	}
	
	public List<DBObject> getSubscriptionIndividualsByDate(String clientId,String subscriptionId,Date date){
		SchemaSubscriptionIndividuals schemaSubscriptionIndividuals = this.schemaSubscriptionIndividualsDAO.getBySubscriptionId(subscriptionId);
		List<String> matchedIndividualsIds = schemaSubscriptionIndividuals.getMatchedIndividualsIds();
		List<DBObject> subscriptionIndividuals = new ArrayList<>();
		DBCollection collection = MongoDatastore.getPublishDatastore().getDB().getCollection(schemaSubscriptionIndividuals.getSchemaSubscription().getSchema().getName());
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

	public String getJSONSubscriptionIndividuals(String clientId,String subscriptionId){
		List<DBObject> subscriptionIndividuals = this.getSubscriptionIndividuals(clientId,subscriptionId).stream().map(this.individualToBeanConverter::convert).collect(Collectors.toList());
		return JSON.serialize(subscriptionIndividuals);

	}


	public List<DBObject> getSubscriptionIndividuals(String clientId,String subscriptionId){
		Date nextCheckingDate = new Date();
		SchemaSubscriptionIndividuals schemaSubscriptionIndividuals = this.schemaSubscriptionIndividualsDAO.getBySubscriptionId(subscriptionId);
		List<DBObject> subscriptionIndividuals = new ArrayList<>();
		if(schemaSubscriptionIndividuals != null){
			ClientCheckpoint clientCheckpoint = this.clientCheckpointDAO.getCheckPointByClientIdAndSubscriptionId(clientId,subscriptionId);
			List<String> matchedIndividualsIds = schemaSubscriptionIndividuals.getMatchedIndividualsIds();

			DBCollection collection = MongoDatastore.getPublishDatastore().getDB().getCollection(schemaSubscriptionIndividuals.getSchemaSubscription().getSchema().getName());
			int i = 0;
			while(i < matchedIndividualsIds.size()) {
				String individualId = matchedIndividualsIds.get(i++);
				DBObject query = new BasicDBObject();
				query.put("_id", new ObjectId(individualId));
				query.put("creationDate",new BasicDBObject("$gte",clientCheckpoint.getCheckingDate()));
				if(collection.find(query).hasNext()) {
					DBObject individual = collection.find(query).next();
					individual.put("id",individualId);
					subscriptionIndividuals.add(individual);
				}
			}
			clientCheckpoint.setCheckingDate(nextCheckingDate);
			this.clientCheckpointDAO.save(clientCheckpoint);
		}else{
			log.info("Invalid subscription details");
		}
		return subscriptionIndividuals;
	}
}
