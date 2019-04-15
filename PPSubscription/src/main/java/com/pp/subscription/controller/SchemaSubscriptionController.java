package com.pp.subscription.controller;

import com.mongodb.DBObject;
import com.pp.database.model.subscription.SchemaSubscription;
import com.pp.subscription.service.SchemaSubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/subscription")
public class SchemaSubscriptionController {

	
	@Autowired
	private SchemaSubscriptionService schemaSubscriptionService;
	
	
	@RequestMapping(path = "/create",method=RequestMethod.POST)
	public void createSubscription(@RequestBody SchemaSubscription schemaSubscription) {
		this.schemaSubscriptionService.createSubscription(schemaSubscription);
	}
	
	@RequestMapping("/all")
	public List<SchemaSubscription> getAllSubscriptions(){
		return this.schemaSubscriptionService.getAllSubscriptions();
	}

	
	@RequestMapping(path="/individuals/{clientId}/{subscriptionId}/{date}",method=RequestMethod.GET)
	public List<DBObject> getSubscriptionIndividualsByTimeStamp(@PathVariable String clientId, @PathVariable String subscriptionId, @PathVariable Date date){
		return this.schemaSubscriptionService.getSubscriptionIndividualsByDate(clientId,subscriptionId,date);
	}

	@RequestMapping(path="/individuals/{clientId}/{subscriptionId}",method=RequestMethod.GET)
	@ResponseBody
	public String getSubscriptionIndividuals(@PathVariable String clientId, @PathVariable String subscriptionId){
		return this.schemaSubscriptionService.getJSONSubscriptionIndividuals(clientId,subscriptionId);
	}
}
