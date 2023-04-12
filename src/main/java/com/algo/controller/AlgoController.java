package com.algo.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.algo.constants.Constants;
import com.algo.constants.UtilityClass;
import com.algo.kiteConnectInteface.KiteConnectInterface;
import com.algo.services.DaoService;
import com.algo.services.OptionStrikesIntrument;
import com.algo.services.TickerService;
import com.algo.strategy.CommonStraddleStrategy;
import com.algo.strategy.StrategyFactory;
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
	DaoService daoService;
	@Autowired
	StrategyFactory strategyFactory;
	@Autowired
	TickerService tickerService;

	Logger log = (Logger) LoggerFactory.getLogger(AlgoController.class);

	@GetMapping(value = "/instruments")
	public List<Instrument> getAllInstrument() {
		List<Instrument> instrumentList = null;
		try {
//			instrumentList = optionStrikesIntrument.showAllInstrumentsOfGivenUnderLyingAssert(Constants.NIFTY_INDEX);	
			instrumentList = optionStrikesIntrument
					.showAllInstrumentsOfGivenUnderLyingAssertAndStrikes(Constants.NIFTY_INDEX, "17550");
		} catch (JSONException | IOException | KiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return instrumentList;
	}
	
	@GetMapping(value = "/setInstruments")
	public String setInstruments() {
		String status = "";
		try {	
			status = daoService.getAndSaveIntrumentDetails(Constants.NIFTY_INDEX);
		} catch (JSONException | IOException | KiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return status;
	}

	@GetMapping(value = "/setStraddle")
	public String setStraddleStrategy() {
		String status = "";
		log.info("In setStraddleStrategy API Start");
		try {
			CommonStraddleStrategy commonStraddleStrategy = strategyFactory.getStrategyService("singleStraddleStrategy");
			String strikePrice = UtilityClass.getStrikePrice(optionStrikesIntrument.getLTP(Constants.NIFTY_TOKEN));
			status = commonStraddleStrategy.setStraddleStrategy(strikePrice);
		} catch (KiteException | Exception e) {
			log.error("Exception on setStraddleStrategy", e);
		}
		log.info("In setStraddleStrategy API End");
		return status;
	}

	@GetMapping(value = "/setDoubleStraddle")
	public String setDoubleStraddleStrategy() {
		String status = "";
		log.info("In setDoubleStraddleStrategy API Start");
		try {
			CommonStraddleStrategy commonStraddleStrategy = strategyFactory.getStrategyService("doubleStraddleStrategy");
			status = commonStraddleStrategy.setStraddleStrategy("");
		} catch (KiteException | Exception e) {
			log.error("Exception on setStraddleStrategy", e);
		}
		log.info("In setDoubleStraddleStrategy API End");
		return status;
	}

	@GetMapping(value = "/setIronButterFlyWithSL")
	public String setIronButterFlyStrategywithSL() {
		String status = "";
		log.info("In setIronButterFlyStrategywithSL API Start");
		try {
		CommonStraddleStrategy commonStraddleStrategy = strategyFactory.getStrategyService("ironButterFlyStraddleStrategy");
		String strikePrice = UtilityClass.getStrikePrice(optionStrikesIntrument.getLTP(Constants.NIFTY_TOKEN));
		status = commonStraddleStrategy.setStraddleStrategy(strikePrice);
		} catch (KiteException | Exception e) {
			log.error("Exception on setStraddleStrategy", e);
		}
		log.info("In setIronButterFlyStrategywithSL API End");
		return status;
	}

	@GetMapping(value = "/optionData/{underLyingAssert}/{strikePrice}")
	public String getOptionDataForGivenInput(@PathVariable String underLyingAssert, @PathVariable String strikePrice) {
		String responseObject = "";
		log.info("In getLastTradedPriceofReqStrikes API Start Params->" + underLyingAssert + ":" + strikePrice);
		try {
			if (!underLyingAssert.equalsIgnoreCase("") && !strikePrice.equalsIgnoreCase("")) {
				responseObject = optionStrikesIntrument.getOptionDataForGivenInput(strikePrice, underLyingAssert,
						Constants.EXPIRY_DAY);
			} else {
				responseObject = "UnderLying Assert and Strike Price Shouldn't be Empty";
			}
		} catch (Exception | KiteException e) {
			log.error("Exception on getLastTradedPriceofReqStrikes", e);
		}
		log.info("In getLastTradedPriceofReqStrikes API End");
		return responseObject;
	}

	@GetMapping(value = "/demoTicker")
	public String demoTicker() {
		String status = "";
		log.info("In demoTicker API Start");
		try {
			ArrayList<Long> tokens = new ArrayList<>();

			tokens.add((long) 738561);// Reliance
//			tokens.add((long) 11330306);

			tickerService.demoCreateKiteTicker(tokens);
		} catch (KiteException | Exception e) {
			log.error("Exception on setStraddleStrategy", e);
		}
		log.info("In demoTicker API End");
		return status;
	}

}
