package com.algo.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.algo.constants.Constants;
import com.algo.constants.UtilityClass;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.Order;

@Service("ironButterFlyStraddleStrategy")
public class IronButterFlyStraddleStrategy extends CommonStraddleStrategy {

	@Override
	public String setStraddleStrategy(String strikePrice) throws KiteException, KiteException, Exception {
		log.info("In StraddleStrategyService.setIronButterFlyStrategywithSL Start");
		String status = "No Data";
		System.out.println("Strike Price->" + strikePrice);
		if (!UtilityClass.isEmptyString(strikePrice)) {
			List<Instrument> currentStrikePriceList = optionStrikesIntrument
					.getWeeklyOptionContractsIntrumentListAtGivenStrikes(Constants.NIFTY_INDEX, Constants.EXPIRY_DAY,
							strikePrice);
			double CE_Premium = optionStrikesIntrument
					.getLTP(String.valueOf(currentStrikePriceList.get(0).getInstrument_token()));
			double PE_Premium = optionStrikesIntrument
					.getLTP(String.valueOf(currentStrikePriceList.get(1).getInstrument_token()));
			double totalPremium = CE_Premium + PE_Premium;

			String roundPremium = UtilityClass.getStrikePrice(totalPremium);

			System.out.println("Round Premiunm->" + roundPremium);

			String CE_Strike_Price = String
					.valueOf(Math.abs(Long.parseLong(roundPremium) + Long.parseLong(strikePrice)));
			String PE_Strike_Price = String
					.valueOf(Math.abs(Long.parseLong(roundPremium) - Long.parseLong(strikePrice)));

			System.out.println("CE_Strike_Price->" + CE_Strike_Price);
			System.out.println("PE_Strike_Price->" + PE_Strike_Price);

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

					tokenTriggeredInstTypeMap.put(currentStrikePrice.getInstrument_token(),
							currentStrikePrice.getInstrument_type());

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

			System.out.println("CE_SYMBOL-->" + CE_Symbol);

			Order CE_order = ordersBucket.placeOrder(Constants.NIFTY_QUANTITY, Constants.ORDER_TYPE_MARKET, CE_Symbol,
					Constants.PRODUCT_MIS, 0, 0, Constants.EXCHANGE_NFO, Constants.TRANSACTION_TYPE_BUY,
					Constants.VARIETY_REGULAR);

			typeAndTradingSymbolMap.put("CE", CE_Symbol);

			// PE Hedge
			String PE_Symbol = optionStrikesIntrument.getWeeklyOptionContractsSymbolListAtGivenStrikesByInstType(
					Constants.NIFTY_INDEX, Constants.EXPIRY_DAY, PE_Strike_Price, Constants.PE_INTRUMENT_TYPE);
			//

			System.out.println("PE_SYMBOL-->" + PE_Symbol);
			Order PE_order = ordersBucket.placeOrder(Constants.NIFTY_QUANTITY, Constants.ORDER_TYPE_MARKET, PE_Symbol,
					Constants.PRODUCT_MIS, 0, 0, Constants.EXCHANGE_NFO, Constants.TRANSACTION_TYPE_BUY,
					Constants.VARIETY_REGULAR);

			typeAndTradingSymbolMap.put("PE", PE_Symbol);

			if (tokenTriggeredInstTypeMap.size() > 0 && tokenSLOrderIdSymbolMap.size() > 0
					&& tokenTriggeredPriceSymbolMap.size() > 0 && typeAndTradingSymbolMap.size() > 0) {
				tickerService.createKiteTickerForIronButterFly(tokenList, tokenTriggeredInstTypeMap,
						tokenSLOrderIdSymbolMap, tokenTriggeredPriceSymbolMap, typeAndTradingSymbolMap);
			}
			status="Success";
		}
		log.info("In StraddleStrategyService.setIronButterFlyStrategywithSL End");
		return status;
	}
}
