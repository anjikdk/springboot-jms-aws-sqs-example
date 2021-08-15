package com.example.springbootjmsawssqsexample.producer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import com.amazon.sqs.javamessaging.SQSMessagingClientConstants;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SQSProducer {
	
	@Value("${sqs.queuename}")
	private String queueName;

	private final JmsTemplate jmsTemplate;
	
	public SQSProducer(@Qualifier("sqsJmsTemplate") JmsTemplate jmsTemplate)
	{
		this.jmsTemplate = jmsTemplate;
	}
	
	public void pushMessageIntoQueue(String message)
	{
		log.info("Queue name: "+ queueName);
		
		jmsTemplate.send(queueName, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				try
				{
					TextMessage textMessage = session.createTextMessage(message);
					textMessage.setStringProperty(SQSMessagingClientConstants.MESSAGE_GROUP_ID, "insert");
					textMessage.setStringProperty("documentType", "insert");
					
					return textMessage;
				}
				catch (Exception e) {
					throw new RuntimeException("Exception....");
				}
			}
		});
	}
}
