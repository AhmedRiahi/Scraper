package com.pp.engine.service;


import com.pp.database.dao.common.DescriptorsPortfolioDAO;
import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.model.engine.DescriptorJob;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.framework.jms.JMSTopics;
import com.pp.framework.jms.sender.PPSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class EngineJobScheduler {

    @Autowired
    private PPSender sender;

    @Autowired
    private DescriptorsPortfolioDAO descriptorsPortfolioDAO;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(50);


    public void scheduleJob(DescriptorWorkflowDataPackage dwdp,long delay){
        log.info("Scheduling job" +dwdp.getDescriptorJob().getName()+" delay: "+delay);
        DescriptorJob descriptorJob = dwdp.getPortfolio().getJobByName(dwdp.getDescriptorJob().getName()).get();
        descriptorJob.setLastCheckingDate(new Date());
        this.descriptorsPortfolioDAO.save(dwdp.getPortfolio());
        this.scheduledExecutorService.schedule(() ->  this.sender.send(JMSTopics.Mozart.PROCESS_DESCRIPTOR, dwdp.getId().toHexString()),delay, TimeUnit.MILLISECONDS);
    }
}
