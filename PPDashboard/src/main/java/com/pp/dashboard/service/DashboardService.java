package com.pp.dashboard.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.pp.database.dao.scrapper.DescriptorDAO;
import com.pp.database.dao.semantic.PPIndividualDAO;
import com.pp.database.dao.semantic.PPIndividualSchemaDAO;
import com.pp.database.kernel.MongoDatastore;
import com.pp.database.model.scrapper.descriptor.DescriptorModel;
import com.pp.database.model.semantic.schema.IndividualSchema;
import com.pp.framework.kafka.KafkaTopics;
import com.pp.framework.kafka.sender.PPSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService{

	@Autowired
	private DescriptorDAO descriptorDAO;
	@Autowired
	private PPIndividualDAO individualDAO;
	@Autowired
	private PPSender sender;
	@Autowired
	private PPIndividualSchemaDAO schemaDAO;


	public List<DBObject> getStagingIndividuals(String descriptorId){
		this.individualDAO.setDatastore(MongoDatastore.getStagingDatastore());
		return this.individualDAO.getStagingDescriptorIndividuals(descriptorId);
	}
	

	public List<DBObject> getPublishedIndividuals(String descriptorId) {
		this.individualDAO.setDatastore(MongoDatastore.getPublishDatastore());
		return this.individualDAO.getPublishedDescriptorIndividuals(descriptorId);
	}


	public void deleteDescriptorIndividuals(String descriptorId) {
		DescriptorModel descriptor = this.descriptorDAO.get(descriptorId);
		descriptor.getIndividualSchemas().stream().forEach(schemaName -> {
			IndividualSchema schema = this.schemaDAO.findByName(schemaName);
			while(schema.getParent() != null){
				DBCollection publishCollection = MongoDatastore.getPublishDatastore().getDB().getCollection(schema.getName());
				DBCollection stagingCollection = MongoDatastore.getStagingDatastore().getDB().getCollection(schema.getName());
				DBObject query = new BasicDBObject();
				query.put("descriptorId",descriptorId);
				publishCollection.remove(query);
				stagingCollection.remove(query);
				schema = schema.getParent();
			}
		});
	}
	

	public void sendCleanRequest(){
		this.sender.send(KafkaTopics.Cleaner.CLEAN+KafkaTopics.IN, "");
	}

}
