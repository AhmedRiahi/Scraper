package com.pp.database.dao.semantic;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.pp.database.kernel.MongoDatastore;
import com.pp.database.kernel.PPDAO;
import com.pp.database.model.semantic.individual.IndividualProperty;
import com.pp.database.model.semantic.individual.PPIndividual;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class PPIndividualDAO extends PPDAO<PPIndividual>{

	public PPIndividualDAO() {
		super(PPIndividual.class);
	}
	
	@Override
	public Key<PPIndividual> save(PPIndividual individual) {
		return super.save(individual);
	}
	
	public void saveConvert(PPIndividual individual) {
		DBObject dbObject = this.individualToDBObject(individual);
		MongoDatastore.getAdvancedDatastore().getDB().getCollection(individual.getSchemaName()).save(dbObject);
		individual.setId((ObjectId) dbObject.get("_id"));
	}
	
	public DBObject individualToDBObject(PPIndividual individual) {
		DBObject dbObject = new BasicDBObject();
		for(IndividualProperty property : individual.getProperties()) {
			dbObject.put(property.getName(),property.getValue());
		}
		dbObject.put("creationDate", new Date());
		dbObject.put("schemaName", individual.getSchemaName());
		dbObject.put("descriptorId", individual.getDescriptorId());
		dbObject.put("workflowId", individual.getWorkflowId());
		dbObject.put("displayString", individual.getDisplayString());
		return dbObject;
	}
	
	public void saveAllConvert(List<PPIndividual> individuals) {
		individuals.stream().forEach(this::saveConvert);
	}
	
	public List<DBObject> getStagingDescriptorIndividuals(String descriptorId){
		Set<String> collections = MongoDatastore.getStagingDatastore().getDB().getCollectionNames().stream().filter(collectionName  -> !collectionName.equalsIgnoreCase("system.users")).collect(Collectors.toSet());;
		return this.getIndividualsBy(MongoDatastore.getStagingDatastore(),collections,"descriptorId", descriptorId);
	}

	public List<DBObject> getStagingWokflowIndividuals(String workflowId){
		Set<String> collections = MongoDatastore.getStagingDatastore().getDB().getCollectionNames().stream().filter(collectionName  -> !collectionName.equalsIgnoreCase("system.users")).collect(Collectors.toSet());;
		return this.getIndividualsBy(MongoDatastore.getStagingDatastore(),collections,"workflowId", workflowId);
	}
	
	public List<DBObject> getPublsihedDescriptorIndividuals(String descriptorId){
		Set<String> collections = MongoDatastore.getPublishDatastore().getDB().getCollectionNames().stream().filter(collectionName  -> !collectionName.equalsIgnoreCase("system.users")).collect(Collectors.toSet());
		return this.getIndividualsBy(MongoDatastore.getPublishDatastore(),collections,"descriptorId", descriptorId);
	}
	
	private List<DBObject> getIndividualsBy(Datastore datastore, Set<String> collections, String field, String id){
		List<DBObject> individuals = new ArrayList<>();
		for(String collection : collections) {
			DBObject query = new BasicDBObject();
			query.put(field, id);
			DBCursor cursor = datastore.getDB().getCollection(collection).find(query);
			while(cursor.hasNext()) {
				DBObject individual = cursor.next();
				individual.put("schemaName",collection);
				individuals.add(individual);
			}
		}
		return individuals;
	}

}
