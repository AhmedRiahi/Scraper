package com.pp.analytics.service;

import com.mongodb.DBObject;
import com.pp.database.dao.semantic.PPIndividualDAO;
import com.pp.database.kernel.MongoDatastore;
import com.pp.database.model.semantic.individual.PPIndividual;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndividualsStagingPublisher {

    @Autowired
    private PPIndividualDAO individualDAO;

    public void publishToStagingArea(List<PPIndividual> individuals) {
        individuals.stream().forEach(this::convertAndSave);
    }

    private void convertAndSave(PPIndividual individual) {
        DBObject dbObject = this.individualDAO.individualToDBObject(individual);
        MongoDatastore.getAdvancedDatastore().getDB().getCollection(individual.getSchemaName()).save(dbObject);
        individual.setId((ObjectId) dbObject.get("_id"));
    }
}
