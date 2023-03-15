package com.algo.kiteConnectInteface;

import java.io.IOException;

import org.json.JSONException;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;

public interface KiteConnectInterface {

	KiteConnect connectZerodha() throws IOException, JSONException, KiteException;

}
