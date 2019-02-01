package com.pp.subscription.kafka;

import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.framework.kafka.KafkaTopics;
import com.pp.framework.kafka.sender.PPSender;
import com.pp.subscription.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SubscriptionReceiver {
	
	private static final Logger log = LoggerFactory.getLogger(SubscriptionReceiver.class);
	
	@Autowired
	private SubscriptionService subscriptionService;
	@Autowired
	private PPSender sender;
	@Autowired
	private DescriptorWorkflowDataPackageDAO dwdpDAO;

	
	
	@KafkaListener(topics = KafkaTopics.Subscription.SCAN_DESCRIPTOR_POPULATION_SUBSCRIPTION+KafkaTopics.IN)
	public void scanDescriptorPopulationSubscription(String processingDataId) {
		DescriptorWorkflowDataPackage dwdp = null;
		try {
			log.info("Subscription received message='{}'"+ processingDataId);
			dwdp = this.dwdpDAO.get(processingDataId);
			this.subscriptionService.scanDescriptorPopulation(dwdp);
			this.sender.send(KafkaTopics.Subscription.SCAN_DESCRIPTOR_POPULATION_SUBSCRIPTION+KafkaTopics.OUT, processingDataId);
		}catch(Throwable e) {
			log.error(e.toString());
            if(dwdp != null){
                dwdp.getDebugInformation().setException(e.getMessage()+"\n"+ Arrays.stream(e.getStackTrace()).map(st -> st.toString()).collect(Collectors.joining("\n")));
                this.dwdpDAO.save(dwdp);
            }else{
                log.error("Enable to set debug information exception because dwdp is null");
            }
			this.sender.send(KafkaTopics.Subscription.SCAN_DESCRIPTOR_POPULATION_SUBSCRIPTION+KafkaTopics.ERROR, processingDataId);
		}
	}
}