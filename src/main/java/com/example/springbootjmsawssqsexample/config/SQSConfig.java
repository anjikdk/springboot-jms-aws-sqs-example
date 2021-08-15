package com.example.springbootjmsawssqsexample.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.util.backoff.ExponentialBackOff;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazon.sqs.javamessaging.SQSSession;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;

@Configuration
public class SQSConfig {

	@Value("${sqs.accesskey}")
	private String accessKey;
	
	@Value("${sqs.secretkey}")
	private String secretKey;
	
	@Value("${sqs.region}")
	private String region;
	
	@Value("${sqs.maxConnections}")
	private int maxConnections;
	
	@Primary
	@Bean(name = "sqsAsyncLambda", destroyMethod = "shutdown")
	public AmazonSQS amazonAyncSQSRotatingSecrets()
	{
		BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKey, secretKey);
		
		return AmazonSQSClientBuilder.standard().withRegion(region)
				.withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials)).build();
	}
	
	@Bean(name = "sqsSyncConnectionFactory")
	public SQSConnectionFactory sqsSyncConnectionFactory(@Qualifier(value = "sqsAsyncLambda") final AmazonSQS amazonSQS)
	{
		return new SQSConnectionFactory(new ProviderConfiguration(), amazonSQS);
	}
	
	@Bean(name = "sqsJmsTemplate")
	public JmsTemplate sqsJMSTemplate(@Qualifier(value = "sqsSyncConnectionFactory") final SQSConnectionFactory sqsConnectionFactory)
	{
		JmsTemplate jmsTemplate = new JmsTemplate(sqsConnectionFactory);
		
		jmsTemplate.setMessageConverter(messageConverter());
		jmsTemplate.setSessionAcknowledgeMode(SQSSession.UNORDERED_ACKNOWLEDGE);
		
		return jmsTemplate;
	}
	
	@Bean(name = "rfSqsMessageConverter")
	public MessageConverter messageConverter()
	{
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		builder.serializationInclusion(JsonInclude.Include.NON_NULL);
		
		MappingJackson2MessageConverter mappingJackson2MessageConverter = new MappingJackson2MessageConverter();
		
		mappingJackson2MessageConverter.setObjectMapper(builder.build());
		mappingJackson2MessageConverter.setTargetType(MessageType.TEXT);
		mappingJackson2MessageConverter.setTypeIdPropertyName("documentType");
		
		return mappingJackson2MessageConverter;
	}
	
	@Bean(name = "rfContainerFactory")
	public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
			@Qualifier(value = "sqsSyncConnectionFactory") final SQSConnectionFactory sqsConnectionFactory)
	{
		final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		
		factory.setConnectionFactory(sqsConnectionFactory);
		factory.setDestinationResolver(new DynamicDestinationResolver());
		factory.setSessionTransacted(false);
		factory.setConcurrency("1-" + maxConnections);
		factory.setSessionAcknowledgeMode(SQSSession.UNORDERED_ACKNOWLEDGE);
		
		ExponentialBackOff backOff = new ExponentialBackOff();
		
		backOff.setInitialInterval(1000);
		backOff.setMultiplier(3);
		backOff.setMaxInterval(60000);
		
		factory.setBackOff(backOff);
		
		return factory;
		
	}
}

