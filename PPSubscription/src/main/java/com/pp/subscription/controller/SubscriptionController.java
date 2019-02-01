package com.pp.subscription.controller;

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
	
	@RequestMapping("/individualsByCount/{subscriptionId}/{count}")
	public List<DBObject> getSubscriptionIndividualsByCount(@PathVariable String subscriptionId,@PathVariable int count){
		return this.subscriptionService.getSubscriptionIndividualsByCount(subscriptionId,count);
	}
	
	@RequestMapping(path="/individualsByDate/{subscriptionId}",method=RequestMethod.POST)
	public List<DBObject> getSubscriptionIndividualsByTimeStamp(@PathVariable String subscriptionId,@RequestBody String date) throws ParseException{
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date javaDate = format.parse(date);
		return this.subscriptionService.getSubscriptionIndividualsByDate(subscriptionId,javaDate);
	}
}
