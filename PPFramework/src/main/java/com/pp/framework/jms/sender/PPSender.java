package com.pp.framework.jms.sender;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class PPSender {

	@Autowired
	private JmsTemplate jmsTemplate;

	public void send(String topic, String message) {
		log.info("sending message :"+message+" to topic : "+topic);
		this.jmsTemplate.convertAndSend(topic,message);
	}
}


	