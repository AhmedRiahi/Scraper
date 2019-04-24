package com.pp.subscription.service;


import com.pp.database.dao.common.DescriptorsPortfolioDAO;
import com.pp.database.dao.engine.DescriptorJobDataSetDAO;
import com.pp.database.dao.subscription.DescriptorSubscriptionDAO;
import com.pp.database.model.common.DescriptorsPortfolio;
import com.pp.database.model.engine.DescriptorJobDataSet;
import com.pp.database.model.subscription.DescriptorSubscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class DescriptorSubscriptionService {

    @Autowired
    private DescriptorSubscriptionDAO descriptorSubscriptionDAO;
    @Autowired
    private DescriptorJobDataSetDAO descriptorJobDataSetDAO;

    public void putUrl(String subscriptionId, String url){
        DescriptorSubscription descriptorSubscription = this.descriptorSubscriptionDAO.get(subscriptionId);
        Optional<DescriptorJobDataSet> descriptorJobDataSetOptional = this.descriptorJobDataSetDAO.findByPortfolioAndJobName(descriptorSubscription.getDescriptorsPortfolio(),descriptorSubscription.getDescriptorJobName());
        if(!descriptorJobDataSetOptional.isPresent()){
            DescriptorJobDataSet descriptorJobDataSet = new DescriptorJobDataSet();
            descriptorJobDataSet.setDescriptorsPortfolio(descriptorSubscription.getDescriptorsPortfolio());
            descriptorJobDataSet.setJobName(descriptorSubscription.getDescriptorJobName());
            descriptorJobDataSet.getToBeProcessedLinks().add(url);
            this.descriptorJobDataSetDAO.save(descriptorJobDataSet);
        }else{
            descriptorJobDataSetOptional.get().getToBeProcessedLinks().add(url);
            this.descriptorJobDataSetDAO.save(descriptorJobDataSetOptional.get());
        }

    }
}
