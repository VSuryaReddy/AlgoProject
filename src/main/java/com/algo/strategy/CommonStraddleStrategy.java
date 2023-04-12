package com.algo.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.algo.constants.Constants;
import com.algo.constants.UtilityClass;
import com.algo.services.OptionStrikesIntrument;
import com.algo.services.OrdersBucket;
import com.algo.strategy.StrategyService;
import com.algo.services.TickerService;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;

@Service("commonStraddleStrategy")
public class CommonStraddleStrategy implements StrategyService {

	@Autowired
	OptionStrikesIntrument optionStrikesIntrument;
	@Autowired
	OrdersBucket ordersBucket;
	@Autowired
	TickerService tickerService;

	Logger log = (Logger) LoggerFactory.getLogger(StrategyService.class);

	@Override
	public String setStraddleStrategy(String strikePrice) throws JSONException, IOException, KiteException, Exception {
	
		log.info("In CommonStraddleStrategy.setStraddleStrategy Start");
		String status = "No Data";
		if (!UtilityClass.isEmptyString(strikePrice)) {
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
							Constants.ORDER_TYPE_MARKET, currentStrikePrice.getTradingsymbol(), Constants.PRODUCT_MIS,
							0, 0, Constants.EXCHANGE_NFO, Constants.TRANSACTION_TYPE_SELL, Constants.VARIETY_REGULAR);
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
				log.info("In CommonStraddleStrategy.setStraddleStrategy End");
				return "Success";
			} else {
				log.info("In CommonStraddleStrategy.setStraddleStrategy End");
				return "Premium Diff Too high ==>" + premiumDiff;
			}
		}
		return status;
	}

}