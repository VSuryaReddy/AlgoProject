package com.algo.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class StrategyFactory {

	@Autowired
	ApplicationContext applicationContext;

	public CommonStraddleStrategy getStrategyService(String serviceName) {
		return (CommonStraddleStrategy) applicationContext.getBean(serviceName);
	}
}
