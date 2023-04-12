package com.algo.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.algo.constants.Constants;
import com.algo.constants.UtilityClass;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.Tick;
import com.zerodhatech.models.Trade;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.ticker.OnConnect;
import com.zerodhatech.ticker.OnDisconnect;
import com.zerodhatech.ticker.OnTicks;

@Service
public class TickerService {

//	@Autowired
//	KiteTicker kiteTicker;

	@Autowired
	KiteConnect kiteConnect;

	@Autowired
	OrdersBucket ordersBucket;

	Logger log = (Logger) LoggerFactory.getLogger(TickerService.class);

	private void onConnectListener(KiteTicker kiteTicker, ArrayList<Long> tokens) {
		kiteTicker.setOnConnectedListener(new OnConnect() {
			@Override
			public void onConnected() {
				kiteTicker.subscribe(tokens);
				kiteTicker.setMode(tokens, KiteTicker.modeFull);
			}
		});
		kiteTicker.connect();
	}
	private void onDisconnectListener(KiteTicker kiteTicker,String strMessage) {
		kiteTicker.setOnDisconnectedListener(new OnDisconnect() {

			@Override
			public void onDisconnected() {
				log.warn(strMessage);
			}
		});
	}
	

	public void createKiteTicker(ArrayList<Long> tokens, Map<Long, String> tokenTradingSymbolMap,
			Map<Long, String> tokenSLOrderIdMap, Map<Long, Double> tokenTriggeredPriceSymbolMap,
			Map<Long, String> tokenMarketOrderIdSymbolMap) throws JSONException, IOException, KiteException, Exception {
		log.info("In TickerService.createKiteTicker Start");
		KiteTicker kiteTicker = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());
        
		//Connect
		onConnectListener(kiteTicker, tokens);

		kiteTicker.setOnTickerArrivalListener(new OnTicks() {
			@Override
			public void onTicks(ArrayList<Tick> ticks) {
				log.info("In onTicks Start: Tick Size->" + ticks.size());
				if (ticks.size() == tokens.size()) {
					if (ticks.get(0) != null) {
						tickerExecuter(ticks.get(0), ticks.get(1), kiteTicker, tokenTradingSymbolMap, tokenSLOrderIdMap,
								tokenTriggeredPriceSymbolMap, tokenMarketOrderIdSymbolMap);
					}
					if (ticks.get(1) != null) {
						tickerExecuter(ticks.get(1), ticks.get(0), kiteTicker, tokenTradingSymbolMap, tokenSLOrderIdMap,
								tokenTriggeredPriceSymbolMap, tokenMarketOrderIdSymbolMap);
					}
				}
				log.info("In onTicks End");
			}
		});
		
		onDisconnectListener(kiteTicker,"*****************Ticker Suspended******************");
		log.info("In TickerService.createKiteTicker End");
	}

	private void tickerExecuter(Tick currentTick, Tick nextTick, KiteTicker kiteTicker,
			Map<Long, String> tokenTradingSymbolMap, Map<Long, String> tokenSLOrderIdMap,
			Map<Long, Double> tokenTriggeredPriceSymbolMap, Map<Long, String> tokenMarketOrderIdSymbolMap) {
		System.out.println(currentTick.getInstrumentToken() + "->" + currentTick.getLastTradedPrice());
		double sLTriggeredPrice = tokenTriggeredPriceSymbolMap.get(currentTick.getInstrumentToken());
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
								tokenMarketOrderIdSymbolMap.get(nextTick.getInstrumentToken()),
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
	
	
	public void createKiteTickerForIronButterFly(ArrayList<Long> tokens, Map<Long, String> tokenTriggeredInstTypeMap,
			Map<Long, String> tokenSLOrderIdSymbolMap, Map<Long, Double> tokenTriggeredPriceSymbolMap,
			Map<String, String> typeAndTradingSymbolMap) {
		log.info("In TickerService.createKiteTickerForIronButterFly Start");
		KiteTicker kiteTicker = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());
		
		//Connect
        onConnectListener(kiteTicker, tokens);
        
		kiteTicker.setOnTickerArrivalListener(new OnTicks() {
			@Override
			public void onTicks(ArrayList<Tick> ticks) {
				log.info("In onTicks Start: Tick Size->" + ticks.size());
				if (ticks.size() > 0) {
					if (ticks.get(0) != null) {
						tickerExecuterForIronButterFly(ticks.get(0), kiteTicker, tokenTriggeredInstTypeMap,
								tokenSLOrderIdSymbolMap, tokenTriggeredPriceSymbolMap, typeAndTradingSymbolMap);
					}
					if (ticks.size() > 1) {
						if (ticks.get(1) != null) {
							tickerExecuterForIronButterFly(ticks.get(1), kiteTicker, tokenTriggeredInstTypeMap,
									tokenSLOrderIdSymbolMap, tokenTriggeredPriceSymbolMap, typeAndTradingSymbolMap);
						}
					}
				}
				log.info("In onTicks End");
			}
		});
		log.info("In TickerService.createKiteTickerForIronButterFly End");
	}

	protected void tickerExecuterForIronButterFly(Tick tick, KiteTicker kiteTicker,
			Map<Long, String> tokenTriggeredInstTypeMap, Map<Long, String> tokenSLOrderIdSymbolMap,
			Map<Long, Double> tokenTriggeredPriceSymbolMap, Map<String, String> typeAndTradingSymbolMap) {

		System.out.println(tick.getInstrumentToken() + "->" + tick.getLastTradedPrice());
		double sLTriggeredPrice = tokenTriggeredPriceSymbolMap.get(tick.getInstrumentToken());

		double lastTradedPrice = tick.getLastTradedPrice();
//		System.out.println(sLTriggeredPrice + "==" + lastTradedPrice);
		log.info("StopLoss Trigger Price -->" + sLTriggeredPrice);
		log.info("Last Traded Price -->" + lastTradedPrice);
		if (lastTradedPrice >= (sLTriggeredPrice - 2.0)) {

			String orderId = tokenSLOrderIdSymbolMap.get(tick.getInstrumentToken());
			Trade trade = getTradesByOrderId(orderId);
			if (trade != null) {
				log.info("Transaction Type -->" + trade.transactionType);
				if (trade.transactionType.equals(Constants.TRANSACTION_TYPE_BUY)) {
					try {
						String instType = tokenTriggeredInstTypeMap.get(tick.getInstrumentToken());
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

	public void demoCreateKiteTicker(ArrayList<Long> tokens)
			throws JSONException, IOException, KiteException, Exception {
		log.info("In TickerService.createKiteTicker Start");
		KiteTicker kiteTicker = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());
		kiteTicker.setOnConnectedListener(new OnConnect() {
			@Override
			public void onConnected() {
				kiteTicker.subscribe(tokens);
				kiteTicker.setMode(tokens, KiteTicker.modeFull);
			}
		});
		kiteTicker.connect();
		final int count = 0;
		kiteTicker.setOnTickerArrivalListener(new OnTicks() {
			@Override
			public void onTicks(ArrayList<Tick> ticks) {

				if (ticks.size() == tokens.size()) {
					if (ticks.get(0) != null) {
						System.out.println("In demo Ticker");
						System.out.println("LTP " + ticks.get(0).getLastTradedPrice());
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.info("Exception while Thread Sleep", e);
					e.printStackTrace();
				}
//				kiteTicker.disconnect();
			}
		});
		kiteTicker.setOnDisconnectedListener(new OnDisconnect() {

			@Override
			public void onDisconnected() {
				log.warn("*****************Ticker Suspended******************");
			}
		});

	}

}
