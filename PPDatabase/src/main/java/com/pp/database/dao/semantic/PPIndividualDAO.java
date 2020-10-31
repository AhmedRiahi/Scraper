package com.pp.database.dao.semantic;

import com.mongodb.*;
import com.pp.database.kernel.MongoDatastore;
import com.pp.database.kernel.PPDAO;
import com.pp.database.model.semantic.individual.properties.IndividualBaseProperty;
import com.pp.database.model.semantic.individual.properties.IndividualEmbeddedProperty;
import com.pp.database.model.semantic.individual.properties.IndividualListProperty;
import com.pp.database.model.semantic.individual.properties.IndividualSimpleProperty;
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
public class PPIndividualDAO extends PPDAO<PPIndividual> {

    public PPIndividualDAO() {
        super(PPIndividual.class);
    }


    public DBObject individualToDBObject(PPIndividual individual) {
        DBObject dbObject = this.getDbObject(individual);
        dbObject.put("creationDate", new Date());
        dbObject.put("schemaName", individual.getSchemaName());
        dbObject.put("descriptorId", individual.getDescriptorId());
        dbObject.put("workflowId", individual.getWorkflowId());
        dbObject.put("displayString", individual.getDisplayString());
        dbObject.put("previousVersion", individual.getPreviousVersion());
        dbObject.put("version", individual.getVersion());
        return dbObject;
    }

    public DBObject getDbObject(PPIndividual individual) {
        DBObject dbObject = new BasicDBObject();
        if (individual.getId() != null) {
            dbObject.put("_id", individual.getId());
        }
        for (IndividualBaseProperty property : individual.getProperties()) {
            if (property instanceof IndividualSimpleProperty) {
                IndividualSimpleProperty individualSimpleProperty = (IndividualSimpleProperty) property;
                if (individualSimpleProperty.getReferenceData() != null) {
                    DBObject referenceObject = new BasicDBObject();
                    referenceObject.put("collectionName", individualSimpleProperty.getReferenceData().getCollectionName());
                    referenceObject.put("id", individualSimpleProperty.getReferenceData().getId());
                    dbObject.put(property.getName(), referenceObject);
                } else {
                    dbObject.put(property.getName(), individualSimpleProperty.getValue());
                }
            } else {
                if (property instanceof IndividualEmbeddedProperty) {
                    IndividualEmbeddedProperty individualEmbeddedProperty = (IndividualEmbeddedProperty) property;
                    DBObject dbObject1 = this.getDbObject(individualEmbeddedProperty.getValue());
                    dbObject.put(property.getName(), dbObject1);
                } else {
                    if (property instanceof IndividualListProperty) {
                        IndividualListProperty individualListProperty = (IndividualListProperty) property;
                        List<DBObject> dbObjects = individualListProperty.getValue().stream().map(this::getDbObject).collect(Collectors.toList());
                        dbObject.put(property.getName(), dbObjects);
                    }
                }
            }

        }
        return dbObject;
    }


    public List<DBObject> getStagingDescriptorIndividuals(String descriptorId) {
        Set<String> collections = MongoDatastore.getStagingDatastore().getDB().getCollectionNames().stream().filter(collectionName -> !collectionName.equalsIgnoreCase("system.users")).collect(Collectors.toSet());
        return this.getIndividualsBy(MongoDatastore.getStagingDatastore(), collections, "descriptorId", descriptorId);
    }

    public List<DBObject> getStagingWokflowIndividuals(String workflowId) {
        Set<String> collections = MongoDatastore.getStagingDatastore().getDB().getCollectionNames().stream().filter(collectionName -> !collectionName.equalsIgnoreCase("system.users")).collect(Collectors.toSet());
        return this.getIndividualsBy(MongoDatastore.getStagingDatastore(), collections, "workflowId", workflowId);
    }

    public List<DBObject> getPublishedDescriptorIndividuals(String descriptorId) {
        Set<String> collections = MongoDatastore.getPublishDatastore().getDB().getCollectionNames().stream().filter(collectionName -> !collectionName.equalsIgnoreCase("system.users")).collect(Collectors.toSet());
        return this.getIndividualsBy(MongoDatastore.getPublishDatastore(), collections, "descriptorId", descriptorId);
    }

    private List<DBObject> getIndividualsBy(Datastore datastore, Set<String> collections, String field, String id) {
        List<DBObject> individuals = new ArrayList<>();
        for (String collection : collections) {
            DBObject sortObject = new BasicDBObject();
            sortObject.put("creationDate", -1);
            DBObject query = new BasicDBObject();
            query.put(field, id);
            DBCursor cursor = datastore.getDB().getCollection(collection).find(query).sort(sortObject).limit(100);
            while (cursor.hasNext()) {
                DBObject individual = cursor.next();
                individual.put("schemaName", collection);
                individual.keySet().stream().forEach(key -> {
                    if (individual.get(key) instanceof DBRef) {
                        DBRef ref = (DBRef) individual.get(key);
                        DBCursor refCursor = datastore.getDB().getCollection(ref.getCollectionName()).find(new BasicDBObject("_id", new ObjectId(ref.getId().toString())));
                        if (refCursor.hasNext()) {
                            individual.put(key, refCursor.next());
                        }
                    }

                    if (individual.get(key) instanceof BasicDBList) {
                        BasicDBList list = (BasicDBList) individual.get(key);
                        List<DBObject> references = list.stream().filter(item -> item instanceof DBRef).map(item -> (DBRef) item).map(dbRef ->
                                datastore.getDB().getCollection(dbRef.getCollectionName()).find(new BasicDBObject("_id", new ObjectId(dbRef.getId().toString()))).next()
                        ).collect(Collectors.toList());
                        individual.put(key, references);
                    }
                });

                individuals.add(individual);
            }
        }
        return individuals;
    }

}
