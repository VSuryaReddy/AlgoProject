package com.algo.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.springframework.stereotype.Service;

import com.algo.constants.Constants;
import com.algo.constants.UtilityClass;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;

@Service("doubleStraddleStrategy")
public class DoubleStraddleStrategy extends CommonStraddleStrategy {
	
	@Override
	public String setStraddleStrategy(String strikePriceString) throws JSONException, IOException, KiteException, Exception {
		log.info("In DoubleStraddleStrategy.setStraddleStrategy Start");
		String status = "";
		List<String> strList = new ArrayList<>();
		double ltp =optionStrikesIntrument.getLTP(Constants.NIFTY_TOKEN);	
		String strikePriceStr =  UtilityClass.getStrikePrice(ltp);
		long ltp_Value =(long) ltp;
		log.info("LTP_VALUE->"+ltp_Value);
		strList.add(strikePriceStr);
		Long strikePrice = Long.parseLong(strikePriceStr);
		log.info("STRIKE_Price->"+strikePrice);
		long secondATM = strikePrice - 50;
		if (Math.abs((strikePrice - 50) - ltp_Value) > (Math.abs((strikePrice + 50) - ltp_Value))) {
			secondATM = strikePrice + 50;
		}
		log.info("SECOND_ATM->"+secondATM);
		strList.add(String.valueOf(secondATM));
		//
		if (!UtilityClass.isListEmpty(strList)) {
			for (String strData : strList) {
				status = super.setStraddleStrategy(strData);
			}
		}
		log.info("In DoubleStraddleStrategy.setStraddleStrategy End");
		return status;
	}
}
