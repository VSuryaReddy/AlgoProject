package com.algo.controller;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.algo.constants.Constants;
import com.algo.kiteConnectInteface.KiteConnectInterface;
import com.algo.services.OptionStrikesIntrument;
import com.algo.services.StrategyService;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;

@RestController
public class AlgoController {

	@Autowired
	@Qualifier(Constants.KITE_EXISTED_API_NAME)
	KiteConnectInterface kiteConnectInterface;
	@Autowired
	OptionStrikesIntrument optionStrikesIntrument;
	@Autowired
	StrategyService strategyService;

	Logger log = (Logger) LoggerFactory.getLogger(AlgoController.class);

	@GetMapping(value = "/instruments")
	public List<Instrument> getAllInstrument() {
		List<Instrument> instrumentList = null;
		try {
//			instrumentList = optionStrikesIntrument.showAllInstrumentsOfGivenUnderLyingAssert(Constants.NIFTY_INDEX);	
			instrumentList = optionStrikesIntrument
					.showAllInstrumentsOfGivenUnderLyingAssertAndStrikes(Constants.NIFTY_INDEX, "17400");
		} catch (JSONException | IOException | KiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return instrumentList;
	}

	@GetMapping(value = "/setStraddle")
	public String setStraddleStrategy() {
		String status = "";
		log.info("In setStraddleStrategy API Start");
		try {
			status = strategyService.setStraddleStrategy();
		} catch (KiteException | Exception e) {
			log.error("Exception on setStraddleStrategy",e);
		}
		log.info("In setStraddleStrategy API End");
		return status;
	}
	@GetMapping(value = "/optionData/{underLyingAssert}/{strikePrice}")
	public String getOptionDataForGivenInput(@PathVariable String underLyingAssert,@PathVariable String strikePrice) {
		String responseObject = "";
		log.info("In getLastTradedPriceofReqStrikes API Start Params->"+underLyingAssert+":"+strikePrice);
		try {
			if(!underLyingAssert.equalsIgnoreCase("")&&!strikePrice.equalsIgnoreCase("")) {				
				responseObject = optionStrikesIntrument.getOptionDataForGivenInput(strikePrice,underLyingAssert,Constants.EXPIRY_DAY);
			}else {
				responseObject="UnderLying Assert and Strike Price Shouldn't be Empty";
			}
		} catch (Exception | KiteException e) {
			log.error("Exception on getLastTradedPriceofReqStrikes",e);
		}
		log.info("In getLastTradedPriceofReqStrikes API End");
		return responseObject;
	}
}
