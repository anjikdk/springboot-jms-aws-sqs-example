package com.example.springbootjmsawssqsexample.consumer;

import javax.jms.JMSException;
import javax.jms.Message;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SQSConsumer {
	
	@JmsListener(destination = "${sqs.queuename}", containerFactory = "rfContainerFactory")
	public void readMessgae(@Payload final String message, Message sqsMessage) throws JMSException
	{
		log.info("Received Message from Queue: "+message);
		
		sqsMessage.acknowledge();
	}
}
