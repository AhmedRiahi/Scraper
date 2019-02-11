package com.pp.engine.job;

import com.pp.engine.service.EngineService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DescriptorsProcessingJob implements Job {

	@Autowired
    private EngineService engineService;
	
	@Override
	public void execute(JobExecutionContext context){
		log.info("Executing DescriptorsProcessingJob ...");
        this.engineService.checkScheduledJobs();
	}

}
