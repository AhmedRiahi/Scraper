package com.pp.synthesis.kafka;

import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.framework.kafka.sender.PPSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SynthesisReceiver {

	private static final Logger log = LoggerFactory.getLogger(SynthesisReceiver.class);
	

	@Autowired
	private PPSender sender;
	@Autowired
	private DescriptorWorkflowDataPackageDAO dwdpDAO;
}
