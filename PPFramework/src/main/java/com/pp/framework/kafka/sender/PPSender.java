package com.pp.framework.kafka.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

public class PPSender {

	private static final Logger log = LoggerFactory.getLogger(PPSender.class);
	
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	public void send(String topic, String message) {
		// the KafkaTemplate provides asynchronous send methods returning a Future
	    ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);

	    // register a callback with the listener to receive the result of the send asynchronously
	    future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

	    	@Override
	    	public void onSuccess(SendResult<String, String> result) {
	    		log.info("sent message='{}' to='{}' with offset={}", message,topic,result.getRecordMetadata().offset());
	    	}

	    	@Override
	    	public void onFailure(Throwable ex) {
	    		log.info("unable to send message='{}' with exception {}",message,ex);
	    	}
	    });
	}
}


	