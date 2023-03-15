package com.algo.constants;

import java.util.List;

import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.Trade;

public class UtilityClass {

	public static boolean isEmptyString(String string) {
		if (string != null && string.length() != 0 && !string.equalsIgnoreCase("") && !string.isEmpty()) {
			return false;
		}
		return true;
	}

	public static boolean isOrderEmpty(Order order) {
		if (order != null && !order.orderId.equalsIgnoreCase("")) {
			return false;
		}
		return true;
	}

	public static boolean isTradeListEmpty(List<Trade> list) {
		if (list != null && list.size() != 0 && !list.isEmpty()) {
			return false;
		}
		return true;

	}

	public static boolean isOrderListEmpty(List<Order> list) {
		if (list != null && list.size() != 0 && !list.isEmpty()) {
			return false;
		}
		return true;

	}

	public static double getPriceOfgivenPercentage(double price, int percent) {
		double percentage = 0.01 * percent;
		int SLPrice = (int) (price * percentage);
		double convertedToDuble = SLPrice;
		return convertedToDuble;
	}

	public static double getTargetPrice(String triggeredPriceStr, double targetPrice) {
		int triggeredPrice = (int) Double.parseDouble(triggeredPriceStr);
		return triggeredPrice + targetPrice;
	}

	public static boolean isInstrumentListEmpty(List<Instrument> instrumentList) {
		if (instrumentList != null && instrumentList.size() != 0 && !instrumentList.isEmpty()) {
			return false;
		}
		return true;

	}

}
