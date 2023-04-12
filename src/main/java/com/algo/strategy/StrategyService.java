package com.algo.strategy;

import java.io.IOException;

import org.json.JSONException;

import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;

public interface StrategyService {

	public String setStraddleStrategy(String strikePrice) throws JSONException, IOException, KiteException, Exception;
}
