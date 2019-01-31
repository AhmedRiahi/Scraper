package com.pp.subscription.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.DBObject;
import com.pp.database.model.subscription.SchemaSubscription;
import com.pp.subscription.service.SubscriptionService;

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
