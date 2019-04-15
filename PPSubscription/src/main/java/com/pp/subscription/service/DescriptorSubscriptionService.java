package com.pp.subscription.service;


import com.pp.database.dao.subscription.DescriptorSubscriptionDAO;
import com.pp.database.model.subscription.DescriptorSubscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DescriptorSubscriptionService {

    @Autowired
    private DescriptorSubscriptionDAO descriptorSubscriptionDAO;

    public void putUrl(String subscriptionId, String url){
        DescriptorSubscription descriptorSubscription = this.descriptorSubscriptionDAO.get(subscriptionId);
        descriptorSubscription.getDescriptorsPortfolio().getJobByName(descriptorSubscription.getDescriptorJobName()).get().getToBeProcessedLinks().add(url);
    }
}
