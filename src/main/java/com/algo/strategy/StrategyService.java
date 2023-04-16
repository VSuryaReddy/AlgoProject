package com.algo.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.json.JSONException;

import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;

public interface StrategyService {

	public String setStraddleStrategy(String strikePrice) throws JSONException, IOException, KiteException, Exception;
	
	public void createKiteTicker(ArrayList<Long> tokenList,Map<String, Map> completeMapDetails);
	
	
}
