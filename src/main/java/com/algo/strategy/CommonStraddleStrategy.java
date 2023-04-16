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
import com.algo.model.IntrumentDetails;
import com.algo.services.OptionStrikesIntrument;
import com.algo.services.OrdersBucket;
import com.algo.services.TickerService;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.Tick;
import com.zerodhatech.models.Trade;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.ticker.OnConnect;
import com.zerodhatech.ticker.OnDisconnect;
import com.zerodhatech.ticker.OnTicks;

@Service("commonStraddleStrategy")
public class CommonStraddleStrategy implements StrategyService {

	@Autowired
	OptionStrikesIntrument optionStrikesIntrument;
	@Autowired
	OrdersBucket ordersBucket;
	@Autowired
	TickerService tickerService;
	@Autowired
	KiteConnect kiteConnect;

	Logger log = (Logger) LoggerFactory.getLogger(StrategyService.class);

	@Override
	public String setStraddleStrategy(String strikePrice) throws JSONException, IOException, KiteException, Exception {

		log.info("In CommonStraddleStrategy.setStraddleStrategy Start");
		String status = "No Data";
		if (!UtilityClass.isEmptyString(strikePrice)) {
			List<IntrumentDetails> currentStrikePriceList = optionStrikesIntrument
					.getWeeklyOptionContractsIntrumentListAtGivenStrikes(Constants.NIFTY_INDEX, Constants.EXPIRY_DAY,
							strikePrice);
			double CE_Premium = optionStrikesIntrument
					.getLTP(String.valueOf(currentStrikePriceList.get(0).getInstrumentToken()));
			double PE_Premium = optionStrikesIntrument
					.getLTP(String.valueOf(currentStrikePriceList.get(1).getInstrumentToken()));
			double premiumDiff = Math.abs(CE_Premium - PE_Premium);
			log.info("Strike Price--> " + currentStrikePriceList.get(0).strikePrice);
			log.info("Symbols--> " + currentStrikePriceList.get(0).tradingSymbol + " "
					+ currentStrikePriceList.get(1).tradingSymbol);
			log.info("Premium Difference--> " + premiumDiff);
			if (premiumDiff <= Constants.ELIGIBLE_DIFFERENCE_BTW_PREMIUM) {

				Map<Long, String> tokenTradingSymbolMap = new HashMap<>();
				Map<Long, String> tokenSLOrderIdMap = new HashMap<>();
				Map<Long, String> tokenMarketOrderIdMap = new HashMap<>();
				Map<Long, Double> tokenTriggeredPriceMap = new HashMap<>();
				ArrayList<Long> tokenList = new ArrayList<>();

				Map<String, Map> completeMapDetails = new HashMap<>();
				
				for (IntrumentDetails currentStrikePrice : currentStrikePriceList) {
					Map orderandSLPriceMap = ordersBucket.placeOptionOrder(Constants.NIFTY_QUANTITY,
							Constants.ORDER_TYPE_MARKET, currentStrikePrice.getTradingSymbol(), Constants.PRODUCT_MIS,
							0, 0, Constants.EXCHANGE_NFO, Constants.TRANSACTION_TYPE_SELL, Constants.VARIETY_REGULAR);
					if (orderandSLPriceMap != null) {
						tokenTradingSymbolMap.put(currentStrikePrice.getInstrumentToken(),
								currentStrikePrice.getTradingSymbol());
						tokenMarketOrderIdMap.put(currentStrikePrice.getInstrumentToken(),
								(String) orderandSLPriceMap.get(Constants.MARKET_ORDER_ID));
						tokenSLOrderIdMap.put(currentStrikePrice.getInstrumentToken(),
								(String) orderandSLPriceMap.get(Constants.SL_ORDER_ID));
						tokenTriggeredPriceMap.put(currentStrikePrice.getInstrumentToken(),
								(Double) orderandSLPriceMap.get(Constants.TARGET_PRICE));
						tokenList.add(currentStrikePrice.getInstrumentToken());
					}
				}
				// Trading Symbol Map
				completeMapDetails.put("tradingSymbol", tokenTradingSymbolMap);
				// SLOrderId Map
				completeMapDetails.put("slOrderId", tokenSLOrderIdMap);
				// TriggeredPrice Map
				completeMapDetails.put("triggeredPrice", tokenTriggeredPriceMap);
				// MarketOrderId Map
				completeMapDetails.put("marketOrderId", tokenMarketOrderIdMap);

				if (tokenTradingSymbolMap.size() > 0 && tokenSLOrderIdMap.size() > 0
						&& tokenTriggeredPriceMap.size() > 0 && tokenMarketOrderIdMap.size() > 0) {
					createKiteTicker(tokenList, completeMapDetails);
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

	@Override
	public void createKiteTicker(ArrayList<Long> tokenList, Map<String, Map> completeMapDetails) {

		Map<Long, String> tokenTradingSymbolMap =completeMapDetails.get("tradingSymbol");
		Map<Long, String> tokenSLOrderIdMap =completeMapDetails.get("slOrderId");
		Map<Long, Double> tokenTriggeredPriceMap =completeMapDetails.get("triggeredPrice");
		Map<Long, String> tokenMarketOrderIdMap =completeMapDetails.get("marketOrderId");
		
		KiteTicker kiteTicker = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());
		
		//Connect
		kiteTicker.setOnConnectedListener(new OnConnect() {
			@Override
			public void onConnected() {
				kiteTicker.subscribe(tokenList);
				kiteTicker.setMode(tokenList, KiteTicker.modeFull);
			}
		});
		kiteTicker.connect();
        //----------------------OnConnect--Completed------------------------	

		
		kiteTicker.setOnTickerArrivalListener(new OnTicks() {
			@Override
			public void onTicks(ArrayList<Tick> ticks) {
				log.info("In onTicks Start: Tick Size->" + ticks.size());
				if (ticks.size() == tokenList.size()) {
					if (ticks.get(0) != null) {
						tickerExecuter(ticks.get(0), ticks.get(1), kiteTicker, tokenTradingSymbolMap, tokenSLOrderIdMap,
								tokenTriggeredPriceMap, tokenMarketOrderIdMap);
					}
					if (ticks.get(1) != null) {
						tickerExecuter(ticks.get(1), ticks.get(0), kiteTicker, tokenTradingSymbolMap, tokenSLOrderIdMap,
								tokenTriggeredPriceMap, tokenMarketOrderIdMap);
					}
				}
				log.info("In onTicks End");
			}
		});
		
		kiteTicker.setOnDisconnectedListener(new OnDisconnect() {
			@Override
			public void onDisconnected() {
				log.warn("*****************Ticker Suspended******************");
			}
		});
		
		log.info("In TickerService.createKiteTicker End");
	}
	
	private void tickerExecuter(Tick currentTick, Tick nextTick, KiteTicker kiteTicker,
			Map<Long, String> tokenTradingSymbolMap, Map<Long, String> tokenSLOrderIdMap,
			Map<Long, Double> tokenTriggeredPriceMap, Map<Long, String> tokenMarketOrderIdMap) {
		System.out.println(currentTick.getInstrumentToken() + "->" + currentTick.getLastTradedPrice());
		double sLTriggeredPrice = tokenTriggeredPriceMap.get(currentTick.getInstrumentToken());
		double lastTradedPrice = currentTick.getLastTradedPrice();
//		System.out.println(sLTriggeredPrice + "==" + lastTradedPrice);
		log.info("StopLoss Trigger Price -->" + sLTriggeredPrice);
		log.info("Last Traded Price -->" + lastTradedPrice);
		if (lastTradedPrice >= (sLTriggeredPrice - 2.0)) {
			String orderId = tokenSLOrderIdMap.get(currentTick.getInstrumentToken());
			Trade trade = getTradesByOrderId(orderId);
			if (trade != null) {
				log.info("Transaction Type -->" + trade.transactionType);
				if (trade.transactionType.equals(Constants.TRANSACTION_TYPE_BUY)) {
					try {
						modifyExistedSLOrder(nextTick.getLastTradedPrice(),
								tokenSLOrderIdMap.get(nextTick.getInstrumentToken()),
								tokenMarketOrderIdMap.get(nextTick.getInstrumentToken()),
								tokenTradingSymbolMap.get(nextTick.getInstrumentToken()));
					} catch (KiteException | Exception e) {
						log.info("Exception in modifyExistedSLOrder", e);
						e.printStackTrace();
					}

					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						log.info("Exception while Thread Sleep", e);
						e.printStackTrace();
					}
					kiteTicker.disconnect();
				}
			}
		}
	}
	
	public Trade getTradesByOrderId(String orderId) {
		List<Trade> tradeList;
		try {
			tradeList = kiteConnect.getOrderTrades(orderId);
			if (!UtilityClass.isListEmpty(tradeList)) {
				return tradeList.get(0);
			}
		} catch (JSONException | IOException | KiteException e) {
			log.info("Exception in getTradesByOrderId", e);
		}
		return null;
	}
	
	private void modifyExistedSLOrder(double lastTradedPrice, String sLOrderId, String marketOrderId,
			String tradingSymbol) throws JSONException, IOException, KiteException, Exception {
		log.info("In modifyExistedSLOrder Start");
		double SLPricePrice = UtilityClass.getPriceOfgivenPercentage(lastTradedPrice, Constants.STOP_LOSS_PERCENT);
		Trade trade = getOrderTrades(marketOrderId);
		double targetPrce = UtilityClass.getTargetPrice(trade.averagePrice, SLPricePrice);
		ordersBucket.modifyOrder(getOrderHistory(sLOrderId), Constants.NIFTY_QUANTITY, Constants.ORDER_TYPE_SL,
				tradingSymbol, Constants.PRODUCT_MIS, targetPrce, targetPrce, Constants.EXCHANGE_NFO,
				Constants.TRANSACTION_TYPE_BUY, Constants.VARIETY_REGULAR);
		log.info("In modifyExistedSLOrder End");
	}
	
	public Trade getOrderTrades(String orderId) {
		List<Trade> tradeList = null;
		try {
			tradeList = kiteConnect.getOrderTrades(orderId);
		} catch (KiteException | Exception e) {
			log.info("Exception in getOrderTrades", e);
			e.printStackTrace();
		}
		return tradeList != null ? tradeList.get(0) : null;
	}

	public Order getOrderHistory(String orderId) {
		List<Order> orderData = null;
		try {
			orderData = kiteConnect.getOrderHistory(orderId);
		} catch (KiteException | Exception e) {
			log.info("Exception in getOrderHistory", e);
			e.printStackTrace();
		}
		return orderData != null ? orderData.get(0) : null;
	}
	
}