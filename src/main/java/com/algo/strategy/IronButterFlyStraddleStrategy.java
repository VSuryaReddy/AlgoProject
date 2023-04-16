package com.algo.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.algo.constants.Constants;
import com.algo.constants.UtilityClass;
import com.algo.model.IntrumentDetails;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.Tick;
import com.zerodhatech.models.Trade;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.ticker.OnConnect;
import com.zerodhatech.ticker.OnTicks;

@Service("ironButterFlyStraddleStrategy")
public class IronButterFlyStraddleStrategy extends CommonStraddleStrategy {

	@Override
	public String setStraddleStrategy(String strikePrice) throws KiteException, KiteException, Exception {
		log.info("In StraddleStrategyService.setIronButterFlyStrategywithSL Start");
		String status = "No Data";
		System.out.println("Strike Price->" + strikePrice);
		if (!UtilityClass.isEmptyString(strikePrice)) {
			List<IntrumentDetails> currentStrikePriceList = optionStrikesIntrument
					.getWeeklyOptionContractsIntrumentListAtGivenStrikes(Constants.NIFTY_INDEX, Constants.EXPIRY_DAY,
							strikePrice);
			double CE_Premium = optionStrikesIntrument
					.getLTP(String.valueOf(currentStrikePriceList.get(0).getInstrumentToken()));
			double PE_Premium = optionStrikesIntrument
					.getLTP(String.valueOf(currentStrikePriceList.get(1).getInstrumentToken()));
			double totalPremium = CE_Premium + PE_Premium;

			String roundPremium = UtilityClass.getStrikePrice(totalPremium);

			System.out.println("Round Premiunm->" + roundPremium);

			String CE_Strike_Price = String
					.valueOf(Math.abs(Long.parseLong(roundPremium) + Long.parseLong(strikePrice)));
			String PE_Strike_Price = String
					.valueOf(Math.abs(Long.parseLong(roundPremium) - Long.parseLong(strikePrice)));

			System.out.println("CE_Strike_Price->" + CE_Strike_Price);
			System.out.println("PE_Strike_Price->" + PE_Strike_Price);

			Map<Long, String> tokenSLOrderIdMap = new HashMap<>();
			Map<Long, Double> tokenTriggeredPriceMap = new HashMap<>();
			Map<Long, String> tokenInstTypeMap = new HashMap<>();
			Map<String, String> typeAndTradingSymbolMap = new HashMap<>();
			ArrayList<Long> tokenList = new ArrayList<>();

			Map<String, Map> completeMapDetails = new HashMap<>();

			for (IntrumentDetails intrumentDetails : currentStrikePriceList) {
				Map orderandSLPriceMap = ordersBucket.placeOptionOrder(Constants.NIFTY_QUANTITY,
						Constants.ORDER_TYPE_MARKET, intrumentDetails.getTradingSymbol(), Constants.PRODUCT_MIS, 0, 0,
						Constants.EXCHANGE_NFO, Constants.TRANSACTION_TYPE_SELL, Constants.VARIETY_REGULAR);
				if (orderandSLPriceMap != null) {

					tokenInstTypeMap.put(intrumentDetails.getInstrumentToken(), intrumentDetails.getInstrumentType());

					tokenSLOrderIdMap.put(intrumentDetails.getInstrumentToken(),
							(String) orderandSLPriceMap.get(Constants.SL_ORDER_ID));

					tokenTriggeredPriceMap.put(intrumentDetails.getInstrumentToken(),
							(Double) orderandSLPriceMap.get(Constants.TARGET_PRICE));

					tokenList.add(intrumentDetails.getInstrumentToken());
				}
			}

			// Trading Symbol Map
			completeMapDetails.put("instrumentType", tokenInstTypeMap);
			// SLOrderId Map
			completeMapDetails.put("slOrderId", tokenSLOrderIdMap);
			// TriggeredPrice Map
			completeMapDetails.put("triggeredPrice", tokenTriggeredPriceMap);

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

			completeMapDetails.put("typeTradingSymbol", typeAndTradingSymbolMap);

			if (tokenInstTypeMap.size() > 0 && tokenSLOrderIdMap.size() > 0 && tokenTriggeredPriceMap.size() > 0
					&& typeAndTradingSymbolMap.size() > 0) {
				createKiteTicker(tokenList, completeMapDetails);
			}
			status = "Success";
		}
		log.info("In StraddleStrategyService.setIronButterFlyStrategywithSL End");
		return status;
	}

	@Override
	public void createKiteTicker(ArrayList<Long> tokens, Map<String, Map> completeMapDetails) {
		log.info("In TickerService.createKiteTickerForIronButterFly Start");
		KiteTicker kiteTicker = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());

		Map<Long, String> tokenInstTypeMap = completeMapDetails.get("instrumentType");
		Map<Long, String> tokenSLOrderIdMap = completeMapDetails.get("slOrderId");
		Map<Long, Double> tokenTriggeredPriceMap = completeMapDetails.get("triggeredPrice");
		Map<String, String> typeAndTradingSymbolMap = completeMapDetails.get("typeTradingSymbol");

		// Connect
		kiteTicker.setOnConnectedListener(new OnConnect() {
			@Override
			public void onConnected() {
				kiteTicker.subscribe(tokens);
				kiteTicker.setMode(tokens, KiteTicker.modeFull);
			}
		});
		kiteTicker.connect();
//		-----------------OnConnect Complete-------------------

		kiteTicker.setOnTickerArrivalListener(new OnTicks() {
			@Override
			public void onTicks(ArrayList<Tick> ticks) {
				log.info("In onTicks Start: Tick Size->" + ticks.size());
				if (ticks.size() > 0) {
					if (ticks.get(0) != null) {
						tickerExecuterForIronButterFly(ticks.get(0), kiteTicker, tokenInstTypeMap, tokenSLOrderIdMap,
								tokenTriggeredPriceMap, typeAndTradingSymbolMap);
					}
					if (ticks.size() > 1) {
						if (ticks.get(1) != null) {
							tickerExecuterForIronButterFly(ticks.get(1), kiteTicker, tokenInstTypeMap,
									tokenSLOrderIdMap, tokenTriggeredPriceMap, typeAndTradingSymbolMap);
						}
					}
				}
				log.info("In onTicks End");
			}
		});
		log.info("In TickerService.createKiteTickerForIronButterFly End");
	}

	protected void tickerExecuterForIronButterFly(Tick tick, KiteTicker kiteTicker, Map<Long, String> tokenInstTypeMap,
			Map<Long, String> tokenSLOrderIdMap, Map<Long, Double> tokenTriggeredPriceMap,
			Map<String, String> typeAndTradingSymbolMap) {

		System.out.println(tick.getInstrumentToken() + "->" + tick.getLastTradedPrice());
		double sLTriggeredPrice = tokenTriggeredPriceMap.get(tick.getInstrumentToken());

		double lastTradedPrice = tick.getLastTradedPrice();
//		System.out.println(sLTriggeredPrice + "==" + lastTradedPrice);
		log.info("StopLoss Trigger Price -->" + sLTriggeredPrice);
		log.info("Last Traded Price -->" + lastTradedPrice);
		if (lastTradedPrice >= (sLTriggeredPrice - 2.0)) {

			String orderId = tokenSLOrderIdMap.get(tick.getInstrumentToken());
			Trade trade = getTradesByOrderId(orderId);
			if (trade != null) {
				log.info("Transaction Type -->" + trade.transactionType);
				if (trade.transactionType.equals(Constants.TRANSACTION_TYPE_BUY)) {
					try {
						String instType = tokenInstTypeMap.get(tick.getInstrumentToken());
						String symbol = typeAndTradingSymbolMap.get(instType);
						exitHedge(symbol);
						ArrayList<Long> token = new ArrayList<>();
						token.add(tick.getInstrumentToken());
						kiteTicker.unsubscribe(token);
					} catch (KiteException | Exception e) {
						log.info("Exception in tickerExecuterForIronButterFly", e);
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void exitHedge(String tradingSymbol) throws Exception, KiteException {
		ordersBucket.placeOrder(Constants.NIFTY_QUANTITY, Constants.ORDER_TYPE_MARKET, tradingSymbol,
				Constants.PRODUCT_MIS, 0, 0, Constants.EXCHANGE_NFO, Constants.TRANSACTION_TYPE_SELL,
				Constants.VARIETY_REGULAR);
	}

}
