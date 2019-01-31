package com.pp.dashboard.service;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.pp.dashboard.controller.DescriptorsPortfolioController;
import com.pp.database.model.mozart.JobExecutionHistory;
import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pp.database.dao.crawler.CrawledContentDAO;
import com.pp.database.dao.mozart.JobExecutionHistoryDAO;
import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.dao.scrapper.DescriptorDAO;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.database.model.scrapper.descriptor.DescriptorModel;
import com.pp.framework.kafka.KafkaTopics;
import com.pp.framework.kafka.sender.PPSender;

@Service
public class DescriptorService {

	private static final Logger log = LoggerFactory.getLogger(DescriptorsPortfolioController.class);
	
	@Autowired
	private DescriptorDAO descriptorDAO;
	@Autowired
	private JobExecutionHistoryDAO dehDAO;
	@Autowired
	private DescriptorWorkflowDataPackageDAO dwdpDAO;
	@Autowired
	private CrawledContentDAO crawledContentDAO;
	@Autowired
	private PPSender sender;
	
	private ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
	
	
	public List<DescriptorModel> getAllDescriptors() {
		return this.descriptorDAO.find().asList();
	}


	public void addDescriptor(DescriptorModel descriptorModel) {
		this.descriptorDAO.save(descriptorModel);
	}
	
	public void deleteDescriptor(String descriptorId) {
		this.descriptorDAO.deleteById(new ObjectId(descriptorId));
	}
	
	public void launchDescriptorProcessing(String descriptorId) {
		log.info("Send descriptor execution request");
		sender.send(KafkaTopics.Mozart.PROCESS_DESCRIPTOR, descriptorId);
	}
	
	public String testScript(String scriptText,String scriptInput) {
		try {
			this.engine.put("clContent",scriptInput);
			Object result = engine.eval(scriptText);
			if(result != null) {
				return result.toString();
			}else {
				return "No Result!";
			}
			
		} catch (ScriptException e) {
			return e.getMessage();
		}
	}
	
	public void descriptorErrorsChecked(String descriptorId) {
		DescriptorModel descriptor = this.descriptorDAO.get(descriptorId);
		//descriptor.setCheckingRequired(false);
		this.descriptorDAO.save(descriptor);
	}
	
	public List<JobExecutionHistory> getDescriptorExecutionHistory(String descriptorId){
		return this.dehDAO.getByDescriptorId(descriptorId);
	}
	
	
	public DescriptorWorkflowDataPackage getExecutionHistoryDWDP(String executionHistoryID) {
		JobExecutionHistory deh = this.dehDAO.get(executionHistoryID);
		return deh.getDwdp();
	}
	
	public String getCrawledContent(String crawledContentId) {
		return this.crawledContentDAO.get(crawledContentId).getContents();
	}

	public String getPureCrawledContent(String crawledContentId){
        String contents = this.crawledContentDAO.get(crawledContentId).getContents();
        Document doc = Jsoup.parse(contents);
        doc.select("script").remove();
        return doc.toString();
    }

    public List<ContentListenerModel> getContentListeners(String descriptorId){
	    DescriptorModel descriptorModel = this.descriptorDAO.get(descriptorId);
	    return descriptorModel.getContentListeners();
    }

}
