package com.algo.services;

import java.io.IOException;
import java.util.ArrayList;
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

	public void createKiteTicker(ArrayList<Long> tokens, Map<Long, String> tokenTradingSymbolMap,
			Map<Long, String> tokenSLOrderIdSymbolMap, Map<Long, Double> tokenTriggeredPriceSymbolMap,
			Map<Long, String> tokenMarketOrderIdSymbolMap) throws JSONException, IOException, KiteException, Exception {
		log.info("In TickerService.createKiteTicker Start");
		KiteTicker kiteTicker = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());
		kiteTicker.setOnConnectedListener(new OnConnect() {
			@Override
			public void onConnected() {
				kiteTicker.subscribe(tokens);
				kiteTicker.setMode(tokens, KiteTicker.modeFull);
			}
		});

		kiteTicker.setOnTickerArrivalListener(new OnTicks() {
			@Override
			public void onTicks(ArrayList<Tick> ticks) {
				log.info("In onTicks Start: Tick Size->"+ticks.size());
				if (ticks.size() == tokens.size()) {
					if (ticks.get(0) != null) {
						System.out.println(ticks.get(0).getInstrumentToken() + "->" + ticks.get(0).getLastTradedPrice());
						double sLTriggeredPrice = tokenTriggeredPriceSymbolMap.get(ticks.get(0).getInstrumentToken());
						double lastTradedPrice = ticks.get(0).getLastTradedPrice();
//						System.out.println(sLTriggeredPrice + "==" + lastTradedPrice);
						if (lastTradedPrice >= (sLTriggeredPrice - 2.0)) {
							String orderId = tokenSLOrderIdSymbolMap.get(ticks.get(0).getInstrumentToken());
//							System.out.println("Tick (0) Order Id-" + orderId);
							Trade trade = getTradesByOrderId(orderId);
							if (trade != null) {

//								System.out.println("Tick (0) Order Id From Trade Obj-" + trade.orderId);
								if (trade.transactionType.equals(Constants.TRANSACTION_TYPE_BUY)) {
//									System.out.println("Tick (0) BUY executed" + trade.transactionType);
									try {
										modifyExistedSLOrder(ticks.get(1).getLastTradedPrice(),
												tokenSLOrderIdSymbolMap.get(ticks.get(1).getInstrumentToken()),
												tokenMarketOrderIdSymbolMap.get(ticks.get(1).getInstrumentToken()),
												tokenTradingSymbolMap.get(ticks.get(1).getInstrumentToken()));
									} catch (KiteException | Exception e) {
										log.info("Exception in modifyExistedSLOrder",e);
										e.printStackTrace();
									}

									try {
										Thread.sleep(3000);
									} catch (InterruptedException e) {
										log.info("Exception while Thread Sleep",e);
										e.printStackTrace();
									}
									kiteTicker.disconnect();
								}
							}
						}
					}
					if (ticks.get(1) != null) {
						System.out.println(ticks.get(1).getInstrumentToken() + "->" + ticks.get(1).getLastTradedPrice());
						double sLTriggeredPrice = tokenTriggeredPriceSymbolMap.get(ticks.get(1).getInstrumentToken());
						double lastTradedPrice = ticks.get(1).getLastTradedPrice();
//						System.out.println(sLTriggeredPrice + "==" + lastTradedPrice);
						if (lastTradedPrice >= (sLTriggeredPrice - 2.0)) {
							String orderId = tokenSLOrderIdSymbolMap.get(ticks.get(1).getInstrumentToken());
//							System.out.println("Tick (1) Order Id-" + orderId);
							Trade trade = getTradesByOrderId(orderId);
							if (trade != null) {
//								System.out.println("Tick (1) Order Id From Trade Obj-" + trade.orderId);
								if (trade.transactionType.equals(Constants.TRANSACTION_TYPE_BUY)) {
//									System.out.println("Tick (1) BUY executed" + trade.transactionType);
									try {
										modifyExistedSLOrder(ticks.get(0).getLastTradedPrice(),
												tokenSLOrderIdSymbolMap.get(ticks.get(0).getInstrumentToken()),
												tokenMarketOrderIdSymbolMap.get(ticks.get(0).getInstrumentToken()),
												tokenTradingSymbolMap.get(ticks.get(0).getInstrumentToken()));
									} catch (KiteException | Exception e) {
										log.info("Exception in modifyExistedSLOrder",e);
										e.printStackTrace();
									}

									try {
										Thread.sleep(3000);
									} catch (InterruptedException e) {
										log.info("Exception while Thread Sleep",e);
										e.printStackTrace();
									}
									kiteTicker.disconnect();
								}
							}
						}
					}
				} else {
//					kiteTicker.disconnect();
				}
				log.info("In onTicks End");
			}

		});
		kiteTicker.connect();
		kiteTicker.setOnDisconnectedListener(new OnDisconnect() {

			@Override
			public void onDisconnected() {
				log.warn("*****************Ticker Suspended******************");
			}
		});
		log.info("In TickerService.createKiteTicker End");
	}

	private void modifyExistedSLOrder(double lastTradedPrice, String sLOrderId, String marketOrderId,
			String tradingSymbol) throws JSONException, IOException, KiteException, Exception {
		log.info("In modifyExistedSLOrder Start");
		double SLPricePrice = UtilityClass.getPriceOfgivenPercentage(lastTradedPrice, Constants.STOP_LOSS_PERCENT);
		Trade trade = getOrderTrades(marketOrderId);
		double targetPrce = UtilityClass.getTargetPrice(trade.averagePrice, SLPricePrice);

//		System.out.println("Target Price-" + targetPrce);
//		System.out.println("LTP-" + lastTradedPrice + "$$" + " Trading Syb-" + tradingSymbol);
		ordersBucket.modifyOrder(getOrderHistory(sLOrderId), Constants.NIFTY_QUANTITY, Constants.ORDER_TYPE_SL,
				tradingSymbol, Constants.PRODUCT_MIS, targetPrce, targetPrce, Constants.EXCHANGE_NFO,
				Constants.TRANSACTION_TYPE_BUY, Constants.VARIETY_REGULAR);
		log.info("In modifyExistedSLOrder End");
	}
	
	public Order getOrderHistory(String orderId) {
		List<Order> orderData = null;
		try {
			orderData = kiteConnect.getOrderHistory(orderId);
		} catch (KiteException | Exception e) {
			log.info("Exception in getOrderHistory",e);
			e.printStackTrace();
		}
		return orderData != null ? orderData.get(0) : null;
	}

	public Trade getOrderTrades(String orderId) {
		List<Trade> tradeList = null;
		try {
			tradeList = kiteConnect.getOrderTrades(orderId);
		} catch (KiteException | Exception e) {
			log.info("Exception in getOrderTrades",e);
			e.printStackTrace();
		}
		return tradeList != null ? tradeList.get(0) : null;
	}

	public Trade getTradesByOrderId(String orderId) {
		List<Trade> tradeList;
		try {
			tradeList = kiteConnect.getOrderTrades(orderId);
			if (!UtilityClass.isTradeListEmpty(tradeList)) {
				return tradeList.get(0);
			}
		} catch (JSONException | IOException | KiteException e) {
			log.info("Exception in getTradesByOrderId",e);
		}

		return null;
	}

}
