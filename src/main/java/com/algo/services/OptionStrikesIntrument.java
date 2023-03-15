package com.algo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.algo.constants.UtilityClass;
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

	Logger log = (Logger) LoggerFactory.getLogger(OptionStrikesIntrument.class);

	public List<Instrument> getAllInstruments() throws JSONException, IOException, KiteException {
		log.info("In getAllInstruments Start");
		List<Instrument> nseInstruments = kiteConnect.getInstruments("NFO");
		// System.out.println(nseInstruments.size());
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

	public List<Instrument> showAllInstrumentsOfGivenUnderLyingAssert(String underLyingAsset)
			throws JSONException, IOException, KiteException {
		List<Instrument> nseInstruments = kiteConnect.getInstruments("NFO");
		List<Instrument> underLylingInstruments = new ArrayList<>();
		// System.out.println(nseInstruments.size());
		for (Instrument instrument : nseInstruments) {

			if (instrument.name.equals(underLyingAsset)) {
				System.out.println("Name :-" + instrument.name + " ; Symbol :-" + instrument.tradingsymbol
						+ " ; Token :-" + instrument.instrument_token + " ; Expiry :-" + instrument.expiry
						+ " ; Last Trade Price :-" + instrument.last_price + " ; Strike-" + instrument.strike
						+ " ; Exchange Token-" + instrument.exchange_token + " ; Tick Size-" + instrument.tick_size
						+ " ; Instrument_Type-" + instrument.instrument_type + " ; Segment-" + instrument.segment
						+ " ; Exchange-" + instrument.exchange + " ; Lot_Size-" + instrument.lot_size);
				underLylingInstruments.add(instrument);
			}
		}
		return underLylingInstruments;
	}

	public List<Instrument> showAllInstrumentsOfGivenUnderLyingAssertAndStrikes(String underLyingAsset,
			String strikePrice) throws JSONException, IOException, KiteException {
		log.info("In showAllInstrumentsOfGivenUnderLyingAssertAndStrikes Start");
		List<Instrument> underLylingAndStrikeInstruments = new ArrayList<>();
		List<Instrument> nseInstruments = kiteConnect.getInstruments("NFO");
		for (Instrument instrument : nseInstruments) {
			if (instrument.name.equals(underLyingAsset) && instrument.strike.equals(strikePrice)) {
				System.out.println("Name :-" + instrument.name + " ; Symbol :-" + instrument.tradingsymbol
						+ " ; Token :-" + instrument.instrument_token + " ; Expiry :-" + instrument.expiry
						+ " ; Last Trade Price :-" + instrument.last_price + " ; Strike-" + instrument.strike
						+ " ; Exchange Token-" + instrument.exchange_token + " ; Tick Size-" + instrument.tick_size
						+ " ; Instrument_Type-" + instrument.instrument_type + " ; Segment-" + instrument.segment
						+ " ; Exchange-" + instrument.exchange + " ; Lot_Size-" + instrument.lot_size);
				underLylingAndStrikeInstruments.add(instrument);
			}
		}
		log.info("In showAllInstrumentsOfGivenUnderLyingAssertAndStrikes End");
		return underLylingAndStrikeInstruments;
	}

	public Long getWeeklyOptionContractsTokenListAtGivenStrikesTypes(String underLyingAsset, int weeklyExpiryDay,
			String strikePrice, String strikeType) throws JSONException, IOException, KiteException {
		List<Instrument> nseInstruments = kiteConnect.getInstruments("NFO");
		Date nextExpDate = expiryDayOfComingWeek.getExpiryDayOfComingWeek(weeklyExpiryDay);
		for (Instrument instrument : nseInstruments) {
			if (instrument.name.equals(underLyingAsset) && instrument.strike.equals(strikePrice)
					&& instrument.instrument_type.equalsIgnoreCase(strikeType)
					&& instrument.expiry.getTime() / 1000 == nextExpDate.getTime() / 1000) {
				return instrument.exchange_token;

			}
		}
		return (long) 0;
	}

	public List<Long> getWeeklyOptionContractsTokenListAtGivenStrikes(String underLyingAsset, int weeklyExpiryDay,
			String strikePrice) throws JSONException, IOException, KiteException {
		List<Instrument> nseInstruments = kiteConnect.getInstruments("NFO");
		List<Long> tokensList = new ArrayList<>();
		Date nextExpDate = expiryDayOfComingWeek.getExpiryDayOfComingWeek(weeklyExpiryDay);
		for (Instrument instrument : nseInstruments) {
			if (instrument.name.equals(underLyingAsset) && instrument.strike.equals(strikePrice)
					&& instrument.expiry.getTime() / 1000 == nextExpDate.getTime() / 1000) {
				tokensList.add(instrument.instrument_token);
			}
		}
		return tokensList;
	}

	public List<Instrument> getWeeklyOptionContractsIntrumentListAtGivenStrikes(String underLyingAsset,
			int weeklyExpiryDay, String strikePrice) throws JSONException, IOException, KiteException {
		List<Instrument> nseInstruments = kiteConnect.getInstruments("NFO");
		List<Instrument> instrumentList = new ArrayList<>();
		Date nextExpDate = expiryDayOfComingWeek.getExpiryDayOfComingWeek(weeklyExpiryDay);
		for (Instrument instrument : nseInstruments) {
			if (instrument.name.equals(underLyingAsset) && instrument.strike.equals(strikePrice)
					&& instrument.expiry.getTime() / 1000 == nextExpDate.getTime() / 1000) {
				instrumentList.add(instrument);

			}
		}
		return instrumentList;
	}

	public List<String> getWeeklyOptionContractsSymbolsListAtGivenStrikes(String underLyingAsset, int weeklyExpiryDay,
			String strikePrice) throws JSONException, IOException, KiteException {
		List<Instrument> nseInstruments = kiteConnect.getInstruments("NFO");
		List<String> tokensList = new ArrayList<>();
		Date nextExpDate = expiryDayOfComingWeek.getExpiryDayOfComingWeek(weeklyExpiryDay);
		for (Instrument instrument : nseInstruments) {
			if (instrument.name.equals(underLyingAsset) && instrument.strike.equals(strikePrice)
					&& instrument.expiry.getTime() / 1000 == nextExpDate.getTime() / 1000) {
				tokensList.add(instrument.tradingsymbol);
			}
		}
		return tokensList;
	}

	public double getLTP(String token) throws KiteException, IOException {
		String[] instruments = { token };
		System.out.println("Token->"+token);
		return kiteConnect.getLTP(instruments).get(token).lastPrice;
	}

	public String getOptionDataForGivenInput(String strikePrice, String underLyingAssert, int expiryDay)
			throws JSONException, IOException, KiteException {
		log.info("In OptionStrikesIntrument.getOptionDataForGivenInput Start");
		List<Instrument> instrumentList = getWeeklyOptionContractsIntrumentListAtGivenStrikes(underLyingAssert,expiryDay, strikePrice);
		List<OptionDetailsOfStrikePrice> optionDetailsOfStrikePriceList = new ArrayList<>();
		double total=0.0;
		double diff=0.0;
		if (!UtilityClass.isInstrumentListEmpty(instrumentList)) {
			for (Instrument instrument : instrumentList) {
				double ltp =	getLTP(String.valueOf(instrument.instrument_token));
				optionDetailsOfStrikePriceList
						.add(OptionDetailsOfStrikePrice.builder().underLyingAssert(instrument.name)
								.strikePrice(instrument.strike).instrumentType(instrument.instrument_type)
								.symbol(instrument.tradingsymbol).token(instrument.instrument_token)
								.lastTradedPrice(ltp).build());
				total =total+ltp;
				diff =Math.abs(diff-ltp);
			}
		}else {
			return "No Data Available for given Input";
		}
		log.info("In OptionStrikesIntrument.getOptionDataForGivenInput End");
		return optionDetailsOfStrikePriceList.toString()+", Difference==>"+diff +", Total==>"+total;
	}
}
