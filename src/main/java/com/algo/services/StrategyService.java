package com.algo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.algo.constants.Constants;
import com.algo.constants.UtilityClass;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.Order;

@Service
public class StrategyService {
	@Autowired
	OptionStrikesIntrument optionStrikesIntrument;
	@Autowired
	OrdersBucket ordersBucket;
	@Autowired
	TickerService tickerService;

	Logger log = (Logger) LoggerFactory.getLogger(StrategyService.class);

	public String setSingleStraddleStrategy() throws JSONException, IOException, KiteException, Exception {
		String status = "";
		String strikePrice = getStrikePrice(optionStrikesIntrument.getLTP(Constants.NIFTY_TOKEN));
		if (!UtilityClass.isEmptyString(strikePrice)) {
			status = setStraddleStrategy(strikePrice);
		}
		return status;
	}

	public String setDoubleStraddleStrategy() throws JSONException, IOException, KiteException, Exception {
		String status = "";
		List<String> strList = new ArrayList<>();
		double ltp =optionStrikesIntrument.getLTP(Constants.NIFTY_TOKEN);	
		String strikePriceStr = getStrikePrice(ltp);
		long ltp_Value =(long) ltp;
		log.info("LTP_VALUE"+ltp_Value);
		strList.add(strikePriceStr);
		Long strikePrice = Long.parseLong(strikePriceStr);
		log.info("STRIKE_Price"+strikePrice);
		long secondATM = strikePrice - 50;
		if (Math.abs((strikePrice - 50) - ltp_Value) > (Math.abs((strikePrice + 50) - ltp_Value))) {
			secondATM = strikePrice + 50;
		}
		log.info("SECOND_ATM"+secondATM);
		strList.add(String.valueOf(secondATM));
		//////////
		if (!UtilityClass.isListEmpty(strList)) {
			for (String strData : strList) {
				status = setStraddleStrategy(strData);
			}
		}
		return status;
	}

	public String setStraddleStrategy(String strikePrice) throws JSONException, IOException, KiteException, Exception {
		log.info("In StraddleStrategyService.setStraddleStrategy Start");

		List<Instrument> currentStrikePriceList = optionStrikesIntrument
				.getWeeklyOptionContractsIntrumentListAtGivenStrikes(Constants.NIFTY_INDEX, Constants.EXPIRY_DAY,
						strikePrice);
		double CE_Premium = optionStrikesIntrument
				.getLTP(String.valueOf(currentStrikePriceList.get(0).getInstrument_token()));
		double PE_Premium = optionStrikesIntrument
				.getLTP(String.valueOf(currentStrikePriceList.get(1).getInstrument_token()));
		double premiumDiff = Math.abs(CE_Premium - PE_Premium);
		log.info("Strike Price--> " + currentStrikePriceList.get(0).strike);
		log.info("Symbols--> " + currentStrikePriceList.get(0).tradingsymbol + " "
				+ currentStrikePriceList.get(1).tradingsymbol);
		log.info("Premium Difference--> " + premiumDiff);
		if (premiumDiff <= Constants.ELIGIBLE_DIFFERENCE_BTW_PREMIUM) {

			Map<Long, String> tokenTradingSymbolMap = new HashMap<>();
			Map<Long, String> tokenSLOrderIdSymbolMap = new HashMap<>();
			Map<Long, String> tokenMarketOrderIdSymbolMap = new HashMap<>();
			Map<Long, Double> tokenTriggeredPriceSymbolMap = new HashMap<>();
			ArrayList<Long> tokenList = new ArrayList<>();
			for (Instrument currentStrikePrice : currentStrikePriceList) {
				Map orderandSLPriceMap = ordersBucket.placeOptionOrder(Constants.NIFTY_QUANTITY,
						Constants.ORDER_TYPE_MARKET, currentStrikePrice.getTradingsymbol(), Constants.PRODUCT_MIS, 0, 0,
						Constants.EXCHANGE_NFO, Constants.TRANSACTION_TYPE_SELL, Constants.VARIETY_REGULAR);
				if (orderandSLPriceMap != null) {
					tokenTradingSymbolMap.put(currentStrikePrice.getInstrument_token(),
							currentStrikePrice.getTradingsymbol());
					tokenMarketOrderIdSymbolMap.put(currentStrikePrice.getInstrument_token(),
							(String) orderandSLPriceMap.get(Constants.MARKET_ORDER_ID));
					tokenSLOrderIdSymbolMap.put(currentStrikePrice.getInstrument_token(),
							(String) orderandSLPriceMap.get(Constants.SL_ORDER_ID));
					tokenTriggeredPriceSymbolMap.put(currentStrikePrice.getInstrument_token(),
							(Double) orderandSLPriceMap.get(Constants.TARGET_PRICE));
					tokenList.add(currentStrikePrice.getInstrument_token());
				}
			}
			if (tokenTradingSymbolMap.size() > 0 && tokenSLOrderIdSymbolMap.size() > 0
					&& tokenTriggeredPriceSymbolMap.size() > 0 && tokenMarketOrderIdSymbolMap.size() > 0) {
				tickerService.createKiteTicker(tokenList, tokenTradingSymbolMap, tokenSLOrderIdSymbolMap,
						tokenTriggeredPriceSymbolMap, tokenMarketOrderIdSymbolMap);
			}
			log.info("In StraddleStrategyService.setStraddleStrategy End");
			return "Success";
		} else {
			log.info("In StraddleStrategyService.setStraddleStrategy End");
			return "Premium Diff Too high ==>" + premiumDiff;
		}
	}

	public String setStraddleWithCombinePremiumSL() throws JSONException, IOException, KiteException, Exception {
		log.info("In StraddleStrategyService.setStraddleWithCombinePremiumSL Start");

		log.info("In StraddleStrategyService.setStraddleWithCombinePremiumSL End");
		return "";
	}

	public String setIronButterFlyStrategywithSL() throws KiteException, KiteException, Exception {
		log.info("In StraddleStrategyService.setIronButterFlyStrategywithSL Start");

		String status = "";
		String strikePrice = getStrikePrice(optionStrikesIntrument.getLTP(Constants.NIFTY_TOKEN));

		System.out.println("Strike Price->"+strikePrice);
		
		List<Instrument> currentStrikePriceList = optionStrikesIntrument
				.getWeeklyOptionContractsIntrumentListAtGivenStrikes(Constants.NIFTY_INDEX, Constants.EXPIRY_DAY,
						strikePrice);
		double CE_Premium = optionStrikesIntrument
				.getLTP(String.valueOf(currentStrikePriceList.get(0).getInstrument_token()));
		double PE_Premium = optionStrikesIntrument
				.getLTP(String.valueOf(currentStrikePriceList.get(1).getInstrument_token()));
		double totalPremium = CE_Premium + PE_Premium;

		String roundPremium = getStrikePrice(totalPremium);

		System.out.println("Round Premiunm->"+roundPremium);
		
		String CE_Strike_Price = String.valueOf(Math.abs(Long.parseLong(roundPremium) + Long.parseLong(strikePrice)));
		String PE_Strike_Price = String.valueOf(Math.abs(Long.parseLong(roundPremium) - Long.parseLong(strikePrice)));

		System.out.println("CE_Strike_Price->"+CE_Strike_Price);
		System.out.println("PE_Strike_Price->"+PE_Strike_Price);
		
		Map<Long, String> tokenSLOrderIdSymbolMap = new HashMap<>();
		Map<Long, Double> tokenTriggeredPriceSymbolMap = new HashMap<>();
		Map<Long, String> tokenTriggeredInstTypeMap = new HashMap<>();
		Map<String, String> typeAndTradingSymbolMap = new HashMap<>();
		ArrayList<Long> tokenList = new ArrayList<>();
		for (Instrument currentStrikePrice : currentStrikePriceList) {
			Map orderandSLPriceMap = ordersBucket.placeOptionOrder(Constants.NIFTY_QUANTITY,
					Constants.ORDER_TYPE_MARKET, currentStrikePrice.getTradingsymbol(), Constants.PRODUCT_MIS, 0, 0,
					Constants.EXCHANGE_NFO, Constants.TRANSACTION_TYPE_SELL, Constants.VARIETY_REGULAR);
			if (orderandSLPriceMap != null) {
				
				tokenTriggeredInstTypeMap.put(currentStrikePrice.getInstrument_token(),currentStrikePrice.getInstrument_type());
				
				tokenSLOrderIdSymbolMap.put(currentStrikePrice.getInstrument_token(),
						(String) orderandSLPriceMap.get(Constants.SL_ORDER_ID));
				
				tokenTriggeredPriceSymbolMap.put(currentStrikePrice.getInstrument_token(),
						(Double) orderandSLPriceMap.get(Constants.TARGET_PRICE));
				
				tokenList.add(currentStrikePrice.getInstrument_token());
			}
		}

		// CE Hedge
		String CE_Symbol = optionStrikesIntrument.getWeeklyOptionContractsSymbolListAtGivenStrikesByInstType(
				Constants.NIFTY_INDEX, Constants.EXPIRY_DAY, CE_Strike_Price, Constants.CE_INTRUMENT_TYPE);
		//
		
		System.out.println("CE_SYMBOL-->"+CE_Symbol);

		Order CE_order = ordersBucket.placeOrder(Constants.NIFTY_QUANTITY, Constants.ORDER_TYPE_MARKET, CE_Symbol,
				Constants.PRODUCT_MIS, 0, 0, Constants.EXCHANGE_NFO, Constants.TRANSACTION_TYPE_BUY,
				Constants.VARIETY_REGULAR);

		typeAndTradingSymbolMap.put("CE", CE_Symbol);
		
		// PE Hedge
		String PE_Symbol = optionStrikesIntrument.getWeeklyOptionContractsSymbolListAtGivenStrikesByInstType(
				Constants.NIFTY_INDEX, Constants.EXPIRY_DAY, PE_Strike_Price, Constants.PE_INTRUMENT_TYPE);
		//
		
		System.out.println("PE_SYMBOL-->"+PE_Symbol);
		Order PE_order = ordersBucket.placeOrder(Constants.NIFTY_QUANTITY, Constants.ORDER_TYPE_MARKET, PE_Symbol,
				Constants.PRODUCT_MIS, 0, 0, Constants.EXCHANGE_NFO, Constants.TRANSACTION_TYPE_BUY,
				Constants.VARIETY_REGULAR);

		typeAndTradingSymbolMap.put("PE", PE_Symbol);
		
		
		if (tokenTriggeredInstTypeMap.size() > 0 && tokenSLOrderIdSymbolMap.size() > 0
				&& tokenTriggeredPriceSymbolMap.size() > 0 && typeAndTradingSymbolMap.size() > 0) {
			tickerService.createKiteTickerForIronButterFly(tokenList, tokenTriggeredInstTypeMap, tokenSLOrderIdSymbolMap,
					tokenTriggeredPriceSymbolMap, typeAndTradingSymbolMap);
		}
		
		log.info("In StraddleStrategyService.setIronButterFlyStrategywithSL End");
		return status;
	}

	private static String getStrikePrice(double lastTradedPrice) {
		String strikePrice = String.valueOf(50 * (Math.round(lastTradedPrice / 50)));
		return strikePrice;
	}

}
