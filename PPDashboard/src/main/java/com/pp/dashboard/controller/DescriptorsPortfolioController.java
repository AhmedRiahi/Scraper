package com.pp.dashboard.controller;

import com.pp.dashboard.service.DescriptorsPortfolioService;
import com.pp.database.model.common.DescriptorsPortfolio;
import com.pp.database.model.mozart.JobExecutionHistory;
import com.pp.framework.kafka.KafkaTopics;
import com.pp.framework.kafka.sender.PPSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portfolio")
public class DescriptorsPortfolioController {

    private static final Logger log = LoggerFactory.getLogger(DescriptorsPortfolioController.class);

    @Autowired
    private DescriptorsPortfolioService descriptorsPortfolioService;
    @Autowired
    private PPSender sender;

    @RequestMapping(path = "/getAll",method= RequestMethod.GET)
    public List<DescriptorsPortfolio> getAll(){
        return this.descriptorsPortfolioService.getAll();
    }

    @RequestMapping(method= RequestMethod.POST)
    public void create(@RequestBody DescriptorsPortfolio descriptorsPortfolio){
        this.descriptorsPortfolioService.create(descriptorsPortfolio);
    }

    @RequestMapping(path = "/{portfolioId}",method= RequestMethod.DELETE)
    public void delete(@PathVariable String portfolioId){
        this.descriptorsPortfolioService.delete(portfolioId);
    }

    @RequestMapping(path = "/launchJob/{portfolioId}/{jobName}",method= RequestMethod.GET)
    public void launchJob(@PathVariable String portfolioId,@PathVariable String jobName){
        log.info("Launching job request {} {}",portfolioId,jobName);
        this.sender.send(KafkaTopics.Engine.LAUNCH_JOB+KafkaTopics.IN,portfolioId+"."+jobName);
    }

}
