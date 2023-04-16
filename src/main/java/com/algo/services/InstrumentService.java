package com.algo.services;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.algo.constants.Constants;
import com.algo.dao.IntrumentDao;
import com.algo.model.IntrumentDetails;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;

@Service
public class InstrumentService {

	@Autowired
	private IntrumentDao intrumentDao;
	@Autowired
	private ExpiryDayOfComingWeek expiryDayOfComingWeek;
	@Autowired
	private KiteConnect kiteConnect;

	public String saveIntrumentDetails(List<IntrumentDetails> instrumentsList) {
		String status = "Data Not Saved";
		if (intrumentDao.findAll().size() > 0) {
			intrumentDao.deleteAll();
		}
		if (intrumentDao.saveAll(instrumentsList).size() > 0) {
			status = "Data was Saved";
		}
		return status;
	}

	public String getAndSaveIntrumentDetails(List<String> underLylingAssert)
			throws JSONException, IOException, KiteException {
		List<Instrument> instrumentList = getInstrumentsFromKiteByUnderLyingAssert(underLylingAssert);
		List<IntrumentDetails> intrumentDetails = setIntrumentDetails(instrumentList);
		String status = saveIntrumentDetails(intrumentDetails);
		return status;
	}

	private List<IntrumentDetails> setIntrumentDetails(List<Instrument> instrumentList) {
		return instrumentList.stream().map(intrument -> {
			return IntrumentDetails.builder().instrumentToken(intrument.instrument_token)
					.tradingSymbol(intrument.tradingsymbol).name(intrument.name)
					.instrumentType(intrument.instrument_type).exchange(intrument.exchange).strikePrice(intrument.strike)
					.lotSize(intrument.lot_size).expiryDay(intrument.expiry).build();
		}).collect(Collectors.toList());
	}
	
	public List<Instrument> getInstrumentsFromKiteByUnderLyingAssert(List<String> underLyingAssert)
			throws JSONException, IOException, KiteException {
		List<Instrument> nseInstruments = kiteConnect.getInstruments("NFO");
		List<Instrument> instrumentsList = new ArrayList<>();
		for (Instrument instrument : nseInstruments) {
			if (underLyingAssert.contains(instrument.name)) {
				instrumentsList.add(instrument);
			}
		}
		return instrumentsList;
	}

	

	public void getInstrumentBy(int weeklyExpiry) throws ParseException {
		Date nextExpDate = expiryDayOfComingWeek.getExpiryDayOfComingWeek(weeklyExpiry);
		List<String> strikePriceList = new ArrayList<>();
		strikePriceList.add("17800");
		strikePriceList.add("17850");

//		List<IntrumentDetails> intrumentDetailsList = intrumentDao.getIntDetByUnderAssertAndExpAndStrPriAndInstType(nextExpDate,Constants.PE_INTRUMENT_TYPE,Constants.NIFTY_INDEX,strikePriceList);
		List<IntrumentDetails> intrumentDetailsList = intrumentDao.getIntDetByUnderAssertAndExpAndStrPrice(nextExpDate,Constants.NIFTY_INDEX,strikePriceList);
		for (IntrumentDetails intrumentDetail : intrumentDetailsList) {
			System.out.println("Strike Price->"+intrumentDetail.strikePrice+" INTRUNEMT_TYPE->"+intrumentDetail.instrumentType+
					 " Expiry Day->"+intrumentDetail.expiryDay);
		}		
	}
}
