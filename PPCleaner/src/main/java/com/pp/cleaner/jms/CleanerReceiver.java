package com.pp.cleaner.jms;

import com.pp.cleaner.service.CleanerService;
import com.pp.framework.jms.JMSTopics;
import com.pp.framework.jms.sender.PPSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
public class CleanerReceiver {
	
	private static final Logger log = LoggerFactory.getLogger(CleanerReceiver.class);
	
	@Autowired
	private PPSender ppSender;
	@Autowired
	private CleanerService cleanerService;



	@JmsListener(destination = JMSTopics.Cleaner.CLEAN+ JMSTopics.IN)
	public void clean() {
		log.info("Cleaner Received cleaning command...");
		this.cleanerService.clean();
		log.info("Cleaner Finished cleaning...");
	}
}