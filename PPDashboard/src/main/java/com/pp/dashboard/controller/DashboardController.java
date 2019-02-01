package com.pp.dashboard.controller;

import com.mongodb.DBObject;
import com.pp.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
	
	@Autowired
	private DashboardService dashboardService;
	
	@RequestMapping("/user")
	public Principal getUser(Principal user) {
		return user;
	}
	
	
	
	@RequestMapping("/getStagingIndividuals/{descriptorId}")
	public List<DBObject> getStaringIndividuals(@PathVariable String descriptorId) {
		return dashboardService.getStagingIndividuals(descriptorId);
	}
	
	@RequestMapping("/getPublishedIndividuals/{descriptorId}")
	public List<DBObject> getPublishedIndividuals(@PathVariable String descriptorId) {
		return dashboardService.getPublishedIndividuals(descriptorId);
	}
	
	
	@RequestMapping(path="/deleteDescriptorIndividuals/{descriptorId}",method=RequestMethod.DELETE)
	public void deleteAllDescriptorIndividuals(@PathVariable String descriptorId) {
		this.dashboardService.deleteDescriptorIndividuals(descriptorId);
	}

	@RequestMapping(path = "/clean",method = RequestMethod.GET)
	public void sendCleanRequest(){
        this.dashboardService.sendCleanRequest();
	}
	
}
