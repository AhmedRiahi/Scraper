package com.pp.subscription.controller;

import ch.qos.logback.core.net.server.Client;
import com.mongodb.DBObject;
import com.pp.database.model.subscription.SchemaSubscription;
import com.pp.subscription.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/subscription")
public class SubscriptionController {

	
	@Autowired
	private SubscriptionService subscriptionService;
	
	
	@RequestMapping(path = "/create",method=RequestMethod.POST)
	public void createSubscription(@RequestBody SchemaSubscription schemaSubscription) {
		this.subscriptionService.createSubscription(schemaSubscription);
	}
	
	@RequestMapping("/all")
	public List<SchemaSubscription> getAllSubscriptions(){
		return this.subscriptionService.getAllSubscriptions();
	}

	
	@RequestMapping(path="/individuals/{clientId}/{subscriptionId}/{date}",method=RequestMethod.GET)
	public List<DBObject> getSubscriptionIndividualsByTimeStamp(@PathVariable String clientId, @PathVariable String subscriptionId, @PathVariable Date date){
		return this.subscriptionService.getSubscriptionIndividualsByDate(clientId,subscriptionId,date);
	}

	@RequestMapping(path="/individuals/{clientId}/{subscriptionId}",method=RequestMethod.GET)
	@ResponseBody
	public String getSubscriptionIndividuals(@PathVariable String clientId, @PathVariable String subscriptionId){
		return this.subscriptionService.getJSONSubscriptionIndividuals(clientId,subscriptionId);
	}
}
