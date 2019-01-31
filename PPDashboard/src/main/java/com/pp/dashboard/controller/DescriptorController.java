package com.pp.dashboard.controller;

import java.util.List;

import com.pp.database.model.mozart.JobExecutionHistory;
import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.pp.dashboard.service.DescriptorService;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.database.model.scrapper.descriptor.DescriptorModel;
import com.pp.framework.dataStructure.Couple;


@RestController
@RequestMapping("/descriptor")
public class DescriptorController {

	@Autowired
	private DescriptorService descriptorService;
	
	
	@RequestMapping("/getAll")
	public List<DescriptorModel> getAll(){
		return this.descriptorService.getAllDescriptors();
	}
	
	@RequestMapping(path="/add",method=RequestMethod.POST)
	public void add(@RequestBody DescriptorModel descriptorModel){
		this.descriptorService.addDescriptor(descriptorModel);
	}
	
	@RequestMapping(path="/delete/{descriptorId}",method=RequestMethod.DELETE)
	public void delete(@PathVariable String descriptorId){
		this.descriptorService.deleteDescriptor(descriptorId);
	}
	
	@RequestMapping("/processDescriptor/{descriptorId}")
	public void processDescriptor(@PathVariable String descriptorId) {
		this.descriptorService.launchDescriptorProcessing(descriptorId);
	}
	
	@RequestMapping(path="/testScript",method=RequestMethod.POST)
	public Couple<String, String> testContentListenerScript(@RequestBody String... params) {
		String result = this.descriptorService.testScript(params[0],params[1]);
		return new Couple<String, String>("result",result);
	}
	
	@RequestMapping(path="/flagAsChecked/{descriptorId}",method=RequestMethod.GET)
	public void descriptorErrorChecked(@PathVariable String descriptorId) {
		this.descriptorService.descriptorErrorsChecked(descriptorId);
	}
	
	
	@RequestMapping(path="/executionHistory/{descriptorId}")
	public List<JobExecutionHistory> getExecutionHistory(@PathVariable String descriptorId){
		return this.descriptorService.getDescriptorExecutionHistory(descriptorId);
	}
	
	
	@RequestMapping(path="/executionHistory/{executionHistoryId}/workflowPackage")
	public DescriptorWorkflowDataPackage getExecutionHistoryDWDP(@PathVariable String executionHistoryId) {
		return this.descriptorService.getExecutionHistoryDWDP(executionHistoryId);
	}
	
	@RequestMapping(path="/crawledContent/{crawledContentId}")
	public String getCrawledContent(@PathVariable String crawledContentId) {
		return this.descriptorService.getPureCrawledContent(crawledContentId);
	}

	@RequestMapping(path="/{descriptorId}/contentListeners")
	public List<ContentListenerModel> getContentListeners(@PathVariable String descriptorId) {
		return this.descriptorService.getContentListeners(descriptorId);
	}
}
