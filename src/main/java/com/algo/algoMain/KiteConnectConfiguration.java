package com.algo.algoMain;

import java.io.IOException;
import java.text.ParseException;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;

@Configuration
public class KiteConnectConfiguration {

	@Autowired
	KiteConnectApi kiteConnectApi;

	@Bean
	public KiteConnect kiteConnect() {
		KiteConnect kiteConnect = null;
		try {
			kiteConnect = kiteConnectApi.connectZerodha();
		} catch (JSONException | IOException | KiteException | ParseException e) {
			e.printStackTrace();
		}
		return kiteConnect;
	}

}
