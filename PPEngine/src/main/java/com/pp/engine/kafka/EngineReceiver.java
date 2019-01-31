package com.pp.engine.kafka;


import com.pp.engine.service.EngineService;
import com.pp.framework.kafka.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

public class EngineReceiver {
	
	private static final Logger log = LoggerFactory.getLogger(EngineReceiver.class);

	@Autowired
    private EngineService engineService;
	
	@KafkaListener(topics = KafkaTopics.Engine.LAUNCH_JOB+KafkaTopics.IN)
	public void launchJob(String portfolioJob) {
		log.info("Engine received message={}", portfolioJob);
        this.engineService.launchPortfolioJobWorkflowProcess(portfolioJob);
	}
}