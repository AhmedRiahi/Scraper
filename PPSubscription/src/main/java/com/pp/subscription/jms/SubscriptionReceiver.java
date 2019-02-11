package com.pp.subscription.jms;

import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.framework.jms.KafkaTopics;
import com.pp.framework.jms.sender.PPSender;
import com.pp.subscription.service.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubscriptionReceiver {
	
	@Autowired
	private SubscriptionService subscriptionService;
	@Autowired
	private PPSender sender;
	@Autowired
	private DescriptorWorkflowDataPackageDAO dwdpDAO;


	@JmsListener(destination = KafkaTopics.Subscription.SCAN_DESCRIPTOR_POPULATION_SUBSCRIPTION+KafkaTopics.IN)
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