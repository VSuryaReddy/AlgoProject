package com.algo.algoMain;

import java.io.IOException;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.algo.constants.Constants;
import com.algo.kiteConnectInteface.KiteConnectInterface;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;

@Configuration
public class KiteConnectConfiguration {
	@Autowired
	@Qualifier(Constants.KITE_EXISTED_API_NAME)
	KiteConnectInterface kiteConnectInterface;

	@Bean
	public KiteConnect kiteConnect() {
		KiteConnect kiteConnect = null;
		try {
			kiteConnect = kiteConnectInterface.connectZerodha();
		} catch (JSONException | IOException | KiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return kiteConnect;
	}

}
