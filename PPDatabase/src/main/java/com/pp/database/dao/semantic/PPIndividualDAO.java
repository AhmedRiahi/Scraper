package com.pp.database.dao.semantic;

import com.mongodb.*;
import com.mongodb.client.model.DBCollectionFindOptions;
import com.pp.database.kernel.MongoDatastore;
import com.pp.database.kernel.PPDAO;
import com.pp.database.model.semantic.individual.IndividualProperty;
import com.pp.database.model.semantic.individual.PPIndividual;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
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
	

	
	public DBObject individualToDBObject(PPIndividual individual) {
		DBObject dbObject = new BasicDBObject();
		for(IndividualProperty property : individual.getProperties()) {
			if(property.getReferenceData() != null){
				DBObject referenceObject = new BasicDBObject();
				referenceObject.put("collectionName",property.getReferenceData().getCollectionName());
				referenceObject.put("id",property.getReferenceData().getId());
				dbObject.put(property.getName(),referenceObject);
			}else{
				dbObject.put(property.getName(),property.getValue());
			}

		}
		dbObject.put("creationDate", new Date());
		dbObject.put("schemaName", individual.getSchemaName());
		dbObject.put("descriptorId", individual.getDescriptorId());
		dbObject.put("workflowId", individual.getWorkflowId());
		dbObject.put("displayString", individual.getDisplayString());
		return dbObject;
	}

	
	public List<DBObject> getStagingDescriptorIndividuals(String descriptorId){
		Set<String> collections = MongoDatastore.getStagingDatastore().getDB().getCollectionNames().stream().filter(collectionName  -> !collectionName.equalsIgnoreCase("system.users")).collect(Collectors.toSet());
		return this.getIndividualsBy(MongoDatastore.getStagingDatastore(),collections,"descriptorId", descriptorId);
	}

	public List<DBObject> getStagingWokflowIndividuals(String workflowId){
		Set<String> collections = MongoDatastore.getStagingDatastore().getDB().getCollectionNames().stream().filter(collectionName  -> !collectionName.equalsIgnoreCase("system.users")).collect(Collectors.toSet());
		return this.getIndividualsBy(MongoDatastore.getStagingDatastore(),collections,"workflowId", workflowId);
	}
	
	public List<DBObject> getPublishedDescriptorIndividuals(String descriptorId){
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
				individual.keySet().stream().forEach(key -> {
					if(individual.get(key) instanceof DBRef){
						DBRef ref = (DBRef) individual.get(key);
						DBCursor refCursor = datastore.getDB().getCollection(ref.getCollectionName()).find(new BasicDBObject("_id",new ObjectId(ref.getId().toString())));
						if(refCursor.hasNext()){
							individual.put(key,refCursor.next());
						}
					}

					if(individual.get(key) instanceof BasicDBList){
						BasicDBList list = (BasicDBList)individual.get(key);
						List<DBObject> references = list.stream().filter(item -> item instanceof DBRef).map(item -> (DBRef) item).map(dbRef ->
								datastore.getDB().getCollection(dbRef.getCollectionName()).find(new BasicDBObject("_id",new ObjectId(dbRef.getId().toString()))).next()
						).collect(Collectors.toList());
						individual.put(key,references);
					}
				});

				individuals.add(individual);
			}
		}
		return individuals;
	}

}
