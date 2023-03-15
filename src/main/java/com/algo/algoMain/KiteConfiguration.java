package com.algo.algoMain;

import java.io.IOException;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.algo.kiteConnectInteface.KiteConnectInterface;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;

import jakarta.annotation.PostConstruct;

@Component
public class KiteConfiguration {

	@Autowired
	@Qualifier("kiteConnectApi")
	KiteConnectInterface kiteConnectInterface;

	@PostConstruct
	public void kiteConfiguration() {
		try {
			kiteConnectInterface.connectZerodha();
		} catch (JSONException | IOException | KiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
