package com.pp.subscription.controller;


import com.pp.subscription.service.DescriptorSubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/descriptorSubscription")
public class DescriptorSubscriptionController {

    @Autowired
    private DescriptorSubscriptionService descriptorSubscriptionService;



    @RequestMapping(path="/putUrl/{subscriptionId}/{url}",method= RequestMethod.GET)
    public void putUrl(@PathVariable String clientId, @PathVariable String subscriptionId,@PathVariable String url){
        this.descriptorSubscriptionService.putUrl(subscriptionId,url);
    }
}
