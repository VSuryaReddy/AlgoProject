package com.algo.strategy;

import java.io.IOException;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.algo.services.StrategyService;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;

@Service("singleStraddleStrategy")
public class SingleStraddleStrategy extends CommonStraddleStrategy {

	Logger log = (Logger) LoggerFactory.getLogger(StrategyService.class);
	@Override
	public String setStraddleStrategy(String strikePrice) throws JSONException, IOException, KiteException, Exception {
		log.info("In SingleStraddleStrategy.setStraddleStrategy Start : StikePrice->" + strikePrice);
		String status = super.setStraddleStrategy(strikePrice);
		log.info("In SingleStraddleStrategy.setStraddleStrategy End");
		return status;
	}

}
