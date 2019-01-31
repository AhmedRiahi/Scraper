package com.pp.cleaner.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

import com.pp.cleaner.service.CleanerService;
import com.pp.framework.kafka.KafkaTopics;
import com.pp.framework.kafka.sender.PPSender;

public class CleanerReceiver {
	
	private static final Logger log = LoggerFactory.getLogger(CleanerReceiver.class);
	
	@Autowired
	private PPSender ppSender;
	@Autowired
	private CleanerService cleanerService;

	
	
	@KafkaListener(topics = KafkaTopics.Cleaner.CLEAN+KafkaTopics.IN)
	public void clean() {
		log.info("Cleaner Received cleaning command...");
		this.cleanerService.clean();
		log.info("Cleaner Finished cleaning...");
	}
}