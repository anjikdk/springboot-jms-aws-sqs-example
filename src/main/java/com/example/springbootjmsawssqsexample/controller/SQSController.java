package com.example.springbootjmsawssqsexample.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.springbootjmsawssqsexample.producer.SQSProducer;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class SQSController
{
	private SQSProducer sqsProducer;
	
	@PostMapping("/message")
	public void message(@RequestBody String message)
	{
		sqsProducer.pushMessageIntoQueue(message);
	}
}
