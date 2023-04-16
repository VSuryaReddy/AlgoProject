package com.algo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.algo.constants.Constants;
import com.algo.constants.UtilityClass;
import com.algo.dao.IntrumentDao;
import com.algo.model.IntrumentDetails;
import com.algo.model.OptionDetailsOfStrikePrice;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;

@Service
public class OptionStrikesIntrument {

	@Autowired
	private ExpiryDayOfComingWeek expiryDayOfComingWeek;
	@Autowired
	private KiteConnect kiteConnect;
	@Autowired
	private IntrumentDao intrumentDao;

	Logger log = (Logger) LoggerFactory.getLogger(OptionStrikesIntrument.class);

	public List<Instrument> getAllInstruments() throws JSONException, IOException, KiteException {
		log.info("In getAllInstruments Start");
		List<Instrument> nseInstruments = kiteConnect.getInstruments("NFO");
		for (Instrument instrument : nseInstruments) {
			System.out.println("Name :-" + instrument.name + " ; Symbol :-" + instrument.tradingsymbol + " ; Token :-"
					+ instrument.instrument_token + " ; Expiry :-" + instrument.expiry + " ; Last Trade Price :-"
					+ instrument.last_price + " ; Strike-" + instrument.strike + " ; Exchange Token-"
					+ instrument.exchange_token + " ; Tick Size-" + instrument.tick_size + " ; Instrument_Type-"
					+ instrument.instrument_type + " ; Segment-" + instrument.segment + " ; Exchange-"
					+ instrument.exchange + " ; Lot_Size-" + instrument.lot_size);
		}
		log.info("In getAllInstruments End");
		return nseInstruments;
	}

	public List<IntrumentDetails> getWeeklyOptionContractsIntrumentListAtGivenStrikes(String underLyingAsset,
			int weeklyExpiryDay, String strikePrice) throws JSONException, IOException, KiteException ,ParseException{
		List<String> strikePriceList = new ArrayList<>();
		strikePriceList.add(strikePrice);
		Date nextExpDate = expiryDayOfComingWeek.getExpiryDayOfComingWeek(weeklyExpiryDay);
		List<IntrumentDetails> intrumentDetailsList = intrumentDao.getIntDetByUnderAssertAndExpAndStrPrice(nextExpDate,Constants.NIFTY_INDEX,strikePriceList);		
		return intrumentDetailsList;
	}


	public String getWeeklyOptionContractsSymbolListAtGivenStrikesByInstType(String underLyingAsset,
			int weeklyExpiryDay, String strikePrice, String type) throws JSONException, IOException, KiteException,ParseException {
		Date nextExpDate = expiryDayOfComingWeek.getExpiryDayOfComingWeek(weeklyExpiryDay);
		List<String> strikePriceList = new ArrayList<>();
		strikePriceList.add(strikePrice);
		List<IntrumentDetails> intrumentDetailsList = intrumentDao.getIntDetByUnderAssertAndExpAndStrPriAndInstType(nextExpDate,Constants.PE_INTRUMENT_TYPE,Constants.NIFTY_INDEX,strikePriceList);
		if(!UtilityClass.isListEmpty(intrumentDetailsList)) {
			return intrumentDetailsList.get(0).tradingSymbol;
		}
		return "";
	}

	

	public String getOptionDataForGivenInput(String strikePrice, String underLyingAssert, int expiryDay)
			throws JSONException, IOException, KiteException ,ParseException{
		log.info("In OptionStrikesIntrument.getOptionDataForGivenInput Start");
		List<IntrumentDetails> intrumentDetailsList = getWeeklyOptionContractsIntrumentListAtGivenStrikes(underLyingAssert,
				expiryDay, strikePrice);
		List<OptionDetailsOfStrikePrice> optionDetailsOfStrikePriceList = new ArrayList<>();
		double total = 0.0;
		double diff = 0.0;
		if (!UtilityClass.isListEmpty(intrumentDetailsList)) {
			for (IntrumentDetails intrumentDetails : intrumentDetailsList) {
				double ltp = getLTP(String.valueOf(intrumentDetails.instrumentToken));
				optionDetailsOfStrikePriceList.add(OptionDetailsOfStrikePrice.builder()
						.underLyingAssert(intrumentDetails.name).strikePrice(intrumentDetails.strikePrice)
						.instrumentType(intrumentDetails.instrumentType).symbol(intrumentDetails.tradingSymbol)
						.token(intrumentDetails.instrumentToken).lastTradedPrice(ltp).build());
				total = total + ltp;
				diff = Math.abs(diff - ltp);
			}
		} else {
			return "No Data Available for given Input";
		}
		log.info("In OptionStrikesIntrument.getOptionDataForGivenInput End");
		return optionDetailsOfStrikePriceList.toString() + ", Difference==>" + diff + ", Total==>" + total;
	}
	
	public double getLTP(String token) throws KiteException, IOException {
		String[] instruments = { token };
		System.out.println("Token->" + token);
		return kiteConnect.getLTP(instruments).get(token).lastPrice;
	}
}
