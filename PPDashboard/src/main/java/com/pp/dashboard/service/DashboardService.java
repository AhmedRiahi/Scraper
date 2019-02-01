package com.pp.dashboard.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.pp.database.dao.scrapper.DescriptorDAO;
import com.pp.database.dao.semantic.PPIndividualDAO;
import com.pp.database.kernel.MongoDatastore;
import com.pp.database.model.scrapper.descriptor.DescriptorModel;
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


	public List<DBObject> getStagingIndividuals(String descriptorId){
		this.individualDAO.setDatastore(MongoDatastore.getStagingDatastore());
		return this.individualDAO.getStagingDescriptorIndividuals(descriptorId);
	}
	

	public List<DBObject> getPublishedIndividuals(String descriptorId) {
		this.individualDAO.setDatastore(MongoDatastore.getPublishDatastore());
		return this.individualDAO.getPublsihedDescriptorIndividuals(descriptorId);
	}


	public void deleteDescriptorIndividuals(String descriptorId) {
		DescriptorModel descriptor = this.descriptorDAO.get(descriptorId);
		descriptor.getIndividualSchemas().stream().forEach(schemaName -> {
			DBCollection publishCollection = MongoDatastore.getPublishDatastore().getDB().getCollection(schemaName);
			DBCollection stagingCollection = MongoDatastore.getStagingDatastore().getDB().getCollection(schemaName);
			DBObject query = new BasicDBObject();
			query.put("descriptorId",descriptorId);
			publishCollection.remove(query);
			stagingCollection.remove(query);
		});
	}
	

	public void sendCleanRequest(){
		this.sender.send(KafkaTopics.Cleaner.CLEAN+KafkaTopics.IN, "");
	}

}
