package com.pp.engine.job;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.pp.database.dao.common.DescriptorsPortfolioDAO;
import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.model.common.DescriptorsPortfolio;
import com.pp.database.model.engine.DescriptorJob;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.engine.service.EngineService;
import org.jolokia.util.DateUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pp.database.dao.scrapper.DescriptorDAO;
import com.pp.database.model.scrapper.descriptor.DescriptorModel;
import com.pp.framework.kafka.KafkaTopics;
import com.pp.framework.kafka.sender.PPSender;
import org.springframework.stereotype.Service;

@Component
public class DescriptorsProcessingJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(DescriptorsProcessingJob.class);

	@Autowired
    private EngineService engineService;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("Executing DescriptorsProcessingJob ...");
        this.engineService.checkScheduledJobs();
	}



}
