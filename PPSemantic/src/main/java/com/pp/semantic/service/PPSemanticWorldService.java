package com.pp.semantic.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.pp.database.dao.semantic.PPIndividualDAO;
import com.pp.database.kernel.MongoDatastore;
import com.pp.database.model.semantic.individual.PPIndividual;
import com.pp.database.model.semantic.schema.IndividualSchema;
import com.pp.framework.kafka.KafkaTopics;
import com.pp.framework.kafka.sender.PPSender;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service

public class PPSemanticWorldService {


	@Autowired
	private PPIndividualDAO individualDAO;
	@Autowired
	private PPSender sender;
	@Autowired
	private SchemaService schemaService;

	
	public void addIndividual(PPIndividual individual) {
		this.individualDAO.setDatastore(MongoDatastore.getStagingDatastore());
		this.individualDAO.save(individual);
		sender.send(KafkaTopics.Analytics.ANALYSE_INDIVIDUAL+KafkaTopics.IN,individual.getStringId());
	}
	
	
	public void deleteIndividual(String schemaName,String id) {
		DBCollection publishCollection = MongoDatastore.getPublishDatastore().getDB().getCollection(schemaName);
		DBCollection stagingCollection = MongoDatastore.getStagingDatastore().getDB().getCollection(schemaName);
		DBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(id));
		publishCollection.remove(query);
		stagingCollection.remove(query);
	}
	
	
	public List<DBObject> getAllSchemaIndividuals(String schemaName) {
		List<DBObject> individuals  = new ArrayList<>();
		List<IndividualSchema> allSchemas = new ArrayList<>();
		allSchemas.add(this.schemaService.getIndividualSchema(schemaName));
		
		while(!allSchemas.isEmpty()) {
			IndividualSchema schema = allSchemas.remove(0);
			individuals.addAll(this.getSchemaIndividuals(schema.getName()));
			allSchemas.addAll(this.schemaService.getSchemaChilds(schemaName));
		}
		
		return individuals;
	}
	
	public List<DBObject> getSchemaIndividuals(String schemaName){
		IndividualSchema schema = this.schemaService.getIndividualSchema(schemaName);
		DBCollection collection = MongoDatastore.getPublishDatastore().getDB().getCollection(schemaName);
		List<DBObject> individuals =  collection.find().toArray();
		individuals.stream().forEach(individual -> {
			individual.put("stringId", individual.get("_id").toString());
			if(!schema.getUniqueProperties().isEmpty()){
				String uniqueValue = individual.get(schema.getUniqueProperties().get(0).getName()).toString();
				individual.put("uniqueProperty", uniqueValue);
			}
			
		});
		return individuals;
	}

	
	public void reloadSchemas(){
		this.schemaService.reloadSchemas();
	}
	
}
