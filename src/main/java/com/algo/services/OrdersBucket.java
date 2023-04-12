package com.algo.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.algo.constants.Constants;
import com.algo.constants.UtilityClass;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.OrderParams;
import com.zerodhatech.models.Trade;

@Service
public class OrdersBucket {

	@Autowired
	private KiteConnect kiteConnect;

	Logger log = (Logger) LoggerFactory.getLogger(OrdersBucket.class);

	public Order placeOrder(int quantity, String orderType, String tradingSymbol, String product, double price,
			double triggerPrice, String exchange, String transactionType, String variety)
			throws Exception, KiteException {
		log.info("In OrdersBucket.placeOrder Start");
		OrderParams orderParams = new OrderParams();
		orderParams.quantity = quantity;
		orderParams.orderType = orderType;
		orderParams.tradingsymbol = tradingSymbol;
		orderParams.product = product;
		orderParams.price = price;
		orderParams.triggerPrice = triggerPrice;
		orderParams.exchange = exchange;
		orderParams.transactionType = transactionType;
		orderParams.validity = Constants.VALIDITY_DAY;
		orderParams.tag = "myTag";

		Order order = kiteConnect.placeOrder(orderParams, variety);
		log.info("In OrdersBucket.placeOrder End, OrderId->", order!=null?order.orderId:null);
		return order;
	}

	public Order modifyOrder(Order Order, int quantity, String orderType, String tradingSymbol, String product,
			double price, double triggerPrice, String exchange, String transactionType, String variety)
			throws Exception, KiteException {
		log.info("In OrdersBucket.modifyOrder Start");
		OrderParams orderParams = new OrderParams();
		orderParams.quantity = quantity;
		orderParams.orderType = orderType;
		orderParams.tradingsymbol = tradingSymbol;
		orderParams.product = product;
		orderParams.price = price;
		orderParams.triggerPrice = triggerPrice;
		orderParams.exchange = exchange;
		orderParams.transactionType = transactionType;
		orderParams.validity = Constants.VALIDITY_DAY;
		orderParams.tag = "myTag";
		Order order = kiteConnect.modifyOrder(Order.orderId, orderParams, Constants.VARIETY_REGULAR);
		log.info("In OrdersBucket.modifyOrder End, OrderId->", order!=null?order.orderId:null);
		return order;
	}

	public Map placeOptionOrder(int quantity, String orderType, String tradingSymbol, String product, double price,
			double triggerPrice, String exchange, String transactionType, String variety)
			throws Exception, KiteException {
		log.info("In OrdersBucket.placeOptionOrder Start");
		Order order = placeOrder(quantity, orderType, tradingSymbol, product, price, triggerPrice, exchange,
				transactionType, variety);
		if (!UtilityClass.isOrderEmpty(order) && transactionType.equalsIgnoreCase(Constants.TRANSACTION_TYPE_SELL)) {
			return setStopLossLimitForExistedOptionOrder(order, quantity, Constants.ORDER_TYPE_SL, tradingSymbol,
					product, exchange, Constants.TRANSACTION_TYPE_BUY, variety);
		}
		log.info("In OrdersBucket.placeOptionOrder End");
		return null;
	}

	private Map setStopLossLimitForExistedOptionOrder(Order order, int quantity, String orderType, String tradingSymbol,
			String product, String exchange, String transactionType, String variety) throws Exception, KiteException {
		log.info("In OrdersBucket.setStopLossLimitForExistedOptionOrder Start");
		String triggeredPrice = getTriggeredPriceOfOrder(order);
		if (!UtilityClass.isEmptyString(triggeredPrice)) {
			Map orderandSLPriceMap = new HashMap();
			orderandSLPriceMap.put(Constants.MARKET_ORDER_ID, order.orderId);
			orderandSLPriceMap.put(Constants.EXECUTE_PRICE, Double.parseDouble(triggeredPrice));
			double SLPricePrice = UtilityClass.getPriceOfgivenPercentage(Double.parseDouble(triggeredPrice),Constants.STOP_LOSS_PERCENT);
			double targetPrice = UtilityClass.getTargetPrice(triggeredPrice, SLPricePrice);
			orderandSLPriceMap.put(Constants.TARGET_PRICE, targetPrice);
			Order orderValue = placeOrder(quantity, orderType, tradingSymbol, product, targetPrice, targetPrice,exchange, transactionType, variety);
			System.out.println("SL Order ID" + orderValue.orderId);
			orderandSLPriceMap.put(Constants.SL_ORDER_ID, orderValue.orderId);
			return orderandSLPriceMap;
		}
		log.info("In OrdersBucket.setStopLossLimitForExistedOptionOrder End");
		return null;
	}

	public String getTriggeredPriceOfOrder(Order order) throws KiteException, IOException {
		List<Trade> trades = kiteConnect.getOrderTrades(order.orderId);
		if (!UtilityClass.isListEmpty(trades)) {
			return trades.get(0).averagePrice;
		}
		return null;
	}

}
