package com.pp.engine.service;


import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.framework.kafka.KafkaTopics;
import com.pp.framework.kafka.sender.PPSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class EngineJobScheduler {

    @Autowired
    private PPSender sender;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(50);


    public void scheduleJob(DescriptorWorkflowDataPackage dwdp,long delay){
        log.info("Scheduling job" +dwdp.getDescriptorJob().getName()+" delay: "+delay);
        this.scheduledExecutorService.schedule(() ->  this.sender.send(KafkaTopics.Mozart.PROCESS_DESCRIPTOR, dwdp.getId().toHexString()),delay, TimeUnit.MILLISECONDS);
    }
}
