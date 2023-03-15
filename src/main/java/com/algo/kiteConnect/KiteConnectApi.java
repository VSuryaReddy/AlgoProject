package com.algo.kiteConnect;

import com.algo.constants.Constants;
import com.algo.kiteConnectInteface.KiteConnectInterface;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service("kiteConnectApi")
public class KiteConnectApi implements KiteConnectInterface {

	public KiteConnect connectZerodha() throws IOException, JSONException, KiteException {
		KiteConnect kiteSdk;

		String req_token = Constants.request_Key;
		// API Key
		String api_key = Constants.api_key;
		// Secret Key
		String sec_key = Constants.secret_Key;

		kiteSdk = new KiteConnect(api_key);
		kiteSdk.setUserId("LP8865");

		User users = kiteSdk.generateSession(req_token, sec_key);

		kiteSdk.setAccessToken(users.accessToken);

		kiteSdk.setPublicToken(users.publicToken);
		
		// Access Token to File
		File accessTokenFile = new File("Access_token.txt");
		if (!accessTokenFile.exists()) {
			accessTokenFile.createNewFile();
		}
		FileOutputStream accessOutStream = new FileOutputStream(accessTokenFile);
		accessOutStream.write(users.accessToken.getBytes());

		// Public Token to File
		File publicTokenFile = new File("Public_token.txt");
		if (!publicTokenFile.exists()) {
			publicTokenFile.createNewFile();
		}
		FileOutputStream publicOutStream = new FileOutputStream(publicTokenFile);
		publicOutStream.write(users.publicToken.getBytes());

		return kiteSdk;
	}

}
