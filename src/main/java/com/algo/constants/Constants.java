package com.algo.constants;

import java.util.Calendar;

public class Constants {

//	https://kite.zerodha.com/connect/login?api_key=r3tlii58pyr7j2lo
	
	// API KEY
	public static final String api_key = "r3tlii58pyr7j2lo";

	// SECRET KEY
	public static final String secret_Key = "kkdf5lmbhr109xxew2wfwz1ywqncxjn2";

	// REQUEST_KEY
	public static final String request_Key = "f6nfkbEFZC1yy5qzaW1zl92VdzMZxr71";
	
	public static final String KITE_EXISTED_API_NAME = "kiteExistedApi";
	
	/** Product types. */
	public static final String PRODUCT_MIS = "MIS";
	public static final String PRODUCT_CNC = "CNC";
	public static final String PRODUCT_NRML = "NRML";

	/** Order types. */
	public static final String ORDER_TYPE_MARKET = "MARKET";
	public static final String ORDER_TYPE_LIMIT = "LIMIT";
	public static final String ORDER_TYPE_SL = "SL";
	public static final String ORDER_TYPE_SLM = "SL-M";

	/** Variety types. */
	public static final String VARIETY_REGULAR = "regular";
	public static final String VARIETY_BO = "bo";
	public static final String VARIETY_CO = "co";
	public static final String VARIETY_AMO = "amo";
	public static final String VARIETY_ICEBERG = "iceberg";

	/** Transaction types. */
	public static final String TRANSACTION_TYPE_BUY = "BUY";
	public static final String TRANSACTION_TYPE_SELL = "SELL";

	/** Position types. */
	public static final String POSITION_DAY = "day";
	public static final String POSITION_OVERNIGHT = "overnight";

	/** Validity types. */
	public static final String VALIDITY_DAY = "DAY";
	public static final String VALIDITY_IOC = "IOC";
	public static final String VALIDITY_TTL = "TTL";

	/** Exchanges. */
	public static final String EXCHANGE_NSE = "NSE";
	public static final String EXCHANGE_BSE = "BSE";
	public static final String EXCHANGE_NFO = "NFO";
	public static final String EXCHANGE_BFO = "BFO";
	public static final String EXCHANGE_MCX = "MCX";
	public static final String EXCHANGE_CDS = "CDS";

//	Programmer define
	/** INDEXES. */
	public static final String NIFTY_INDEX = "NIFTY";
	public static final String BANK_NIFTY_INDEX = "BANKNIFTY";
	public static final String SENSEX_INDEX = "SENSEX";

	public static final int EXPIRY_DAY = Calendar.THURSDAY;

	// Percentage
	public static final int STOP_LOSS_PERCENT = 30;

//	Tokens
	public static final String NIFTY_TOKEN = "256265";
	public static final String BANK_NIFTY_TOKEN = "260105";
	
	public static final int ELIGIBLE_DIFFERENCE_BTW_PREMIUM = 20;
	
	//Quantity
	public static final int NIFTY_QUANTITY = 50;
	public static final int BANK_NIFTY_QUANTITY = 25;
	
	public static final String TARGET_PRICE = "TARGET_PRICE";
	public static final String SL_ORDER_ID = "SL_ORDER_ID";
	public static final String MARKET_ORDER_ID = "MARKET_ORDER_ID";
	

}
