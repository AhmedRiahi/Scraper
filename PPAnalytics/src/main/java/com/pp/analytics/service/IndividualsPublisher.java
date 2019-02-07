package com.pp.analytics.service;

import com.mongodb.*;
import com.pp.database.dao.semantic.PPIndividualDAO;
import com.pp.database.dao.semantic.PPIndividualSchemaDAO;
import com.pp.database.kernel.MongoDatastore;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.database.model.semantic.individual.IndividualProperty;
import com.pp.database.model.semantic.individual.PPIndividual;
import com.pp.database.model.semantic.schema.IndividualSchema;
import com.pp.database.model.semantic.schema.PropertyDefinition;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IndividualsPublisher {

    @Autowired
    private PPIndividualSchemaDAO individualSchemaDAO;
    @Autowired
    private PPIndividualDAO individualDAO;


    public void copyPopulationToPublishArea(DescriptorWorkflowDataPackage dwdp) {
        Map<String,IndividualSchema> schemasMap = dwdp.getSchemasNames().stream().collect(Collectors.toMap(schemaName -> schemaName,schemaName -> this.individualSchemaDAO.findOne("name", schemaName)));
        dwdp.getSchemasNames().forEach(schemaName -> {
            IndividualSchema schema  = schemasMap.get(schemaName);

            DBCollection collection = MongoDatastore.getStagingDatastore().getDB().getCollection(schemaName);
            DBObject query = new BasicDBObject();
            query.put("workflowId", dwdp.getStringId());
            DBCursor cursor = collection.find(query);
            cursor.forEach(dbObject -> {
                this.processObjectReferenceProperties(dbObject,schema);
                MongoDatastore.getPublishDatastore().getDB().getCollection(schemaName).save(dbObject);
            });
        });
    }

    private void processObjectReferenceProperties(DBObject dbObject,IndividualSchema schema){
        schema.getReferenceProperties().stream().forEach(propertyDefinition -> {
            DBObject referenceData = (DBObject)dbObject.get(propertyDefinition.getName());
            if(referenceData !=null){
                DBRef dbRef = new DBRef(referenceData.get("collectionName").toString(),new ObjectId(referenceData.get("id").toString()));
                dbObject.put(propertyDefinition.getName(),dbRef);
            }
        });
    }

    private DBObject getDuplicatedIndividual(PPIndividual individual) {
        IndividualSchema schema = this.individualSchemaDAO.findOne("name", individual.getSchemaName());

        List<PropertyDefinition> uniqueSchemaProperties = schema.getUniqueProperties();
        for (PropertyDefinition property : uniqueSchemaProperties) {
            DBObject query = new BasicDBObject();
            Optional<IndividualProperty> individualProperty = individual.getProperty(property.getName());
            if (individualProperty.isPresent()) {
                query.put(property.getName(), individual.getProperty(property.getName()).get().getValue());
                DBCollection collection = MongoDatastore.getPublishDatastore().getDB().getCollection(schema.getName());
                DBObject dbObject = collection.findOne(query);
                return dbObject;
            } else {
                return null;
            }
        }
        return null;
    }


    public boolean isDuplicatedIndividual(PPIndividual individual) {
        return this.getDuplicatedIndividual(individual) != null;
    }

    public List<PPIndividual> getNoDuplicatedPublishedIndividuals(List<PPIndividual> individuals) {
        List<PPIndividual> cleanIndividuals = new ArrayList<>();
        for (PPIndividual individual : individuals) {
            DBObject duplicateIndividual = this.getDuplicatedIndividual(individual);
            if (duplicateIndividual == null) {
                cleanIndividuals.add(individual);
            } else {
                individual.setId((ObjectId) duplicateIndividual.get("_id"));
            }
        }
        return cleanIndividuals;
    }

    public void publishIndividual(PPIndividual individual) {
        DBCollection collection = MongoDatastore.getPublishDatastore().getDB().getCollection(individual.getSchemaName());
        collection.save(individualDAO.individualToDBObject(individual));
    }
}
