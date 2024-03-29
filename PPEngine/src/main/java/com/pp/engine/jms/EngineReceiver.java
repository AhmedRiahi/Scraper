package com.pp.engine.jms;


import com.pp.engine.service.EngineService;
import com.pp.framework.jms.JMSTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EngineReceiver {

	@Autowired
    private EngineService engineService;

	@JmsListener(destination = JMSTopics.Engine.LAUNCH_JOB+ JMSTopics.IN)
	public void launchJob(String portfolioJob) {
		log.info("Engine received message={}", portfolioJob);
        this.engineService.launchPortfolioJobWorkflowProcess(portfolioJob);
	}
}