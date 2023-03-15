package com.algo.kiteConnect;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.json.JSONException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.algo.constants.Constants;
import com.algo.kiteConnectInteface.KiteConnectInterface;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;

@Component
@Service(Constants.KITE_EXISTED_API_NAME)
public class KiteExistedApi implements KiteConnectInterface {

	@Override
	public KiteConnect connectZerodha() throws IOException, JSONException, KiteException {
		//
		KiteConnect kiteSdk = new KiteConnect(Constants.api_key);
		//
		String accessToken = getAccessTokenFromFile();
		String publicToken = getPublicTokenFromFile();

		//
		kiteSdk.setAccessToken(accessToken);
		kiteSdk.setPublicToken(publicToken);

		return kiteSdk;
	}

	private String getAccessTokenFromFile() throws IOException {

		File file = new File("Access_token.txt");
		FileInputStream accesstokenFile = new FileInputStream(file);

		StringBuffer str = new StringBuffer();
		int tokenChar = accesstokenFile.read();

		while (!(tokenChar == -1)) {
			char c = (char) tokenChar;
			str.append(c);
			tokenChar = accesstokenFile.read();
		}

		return str.toString();
	}

	private String getPublicTokenFromFile() throws IOException {
		File file = new File("Public_token.txt");
		FileInputStream publictokenFile = new FileInputStream(file);

		StringBuffer str = new StringBuffer();
		int tokenChar = publictokenFile.read();

		while (!(tokenChar == -1)) {
			char c = (char) tokenChar;
			str.append(c);
			tokenChar = publictokenFile.read();
		}
		return str.toString();
	}

}
