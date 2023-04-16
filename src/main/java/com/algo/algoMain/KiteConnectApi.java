package com.algo.algoMain;

import com.algo.constants.Constants;
import com.algo.constants.UtilityClass;
import com.algo.model.KiteToken;
import com.algo.services.TokenServie;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.User;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KiteConnectApi {

	@Autowired
	TokenServie tokenServie;

	public KiteConnect connectZerodha() throws IOException, KiteException, ParseException {
		String access_Token = "";
		String public_Token = "";
		KiteConnect kiteSdk = new KiteConnect(Constants.api_key);
//		kiteSdk.setUserId("LP8865");

		KiteToken kiteToken = tokenServie.getKiteToken();

		Date today =  UtilityClass.parseAndChangeDateFormat(new Date());
		Date kiteTokeDate = UtilityClass.parseAndChangeDateFormat(kiteToken.getTodayDate());

		if (kiteToken != null && today.getTime() == kiteTokeDate.getTime()) {
			access_Token = kiteToken.getAccessToken();
			public_Token = kiteToken.getPublicToken();
		} else {
			User users = kiteSdk.generateSession(Constants.request_Key, Constants.secret_Key);
			access_Token = users.accessToken;
			public_Token = users.publicToken;
	        //
			kiteToken.setAccessToken(access_Token);
			kiteToken.setPublicToken(public_Token);
			kiteToken.setTodayDate(new Date());
			tokenServie.saveKiteToken(kiteToken);
		}
		
		kiteSdk.setAccessToken(access_Token);
		kiteSdk.setPublicToken(public_Token);

		return kiteSdk;
	}
	
	 

}
