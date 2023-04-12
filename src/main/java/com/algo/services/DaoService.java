package com.algo.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.algo.dao.IntrumentDao;
import com.algo.model.IntrumentDetails;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;

@Service
public class DaoService {

	@Autowired
	private IntrumentDao intrumentDao;

	@Autowired
	private OptionStrikesIntrument optionStrikesIntrument;

	public String saveIntrumentDetails(List<IntrumentDetails> instrumentsList) {
		String status = "Data Not Saved";
		if(intrumentDao.findAll().size()>0) {
			intrumentDao.deleteAll();
		}
		if (intrumentDao.saveAll(instrumentsList).size() > 0) {
			status = "Data was Saved";
		}
		return status;
	}

	public String getAndSaveIntrumentDetails(String underLylingAssert)
			throws JSONException, IOException, KiteException {
		List<Instrument> instrumentList = optionStrikesIntrument.getInstrumentsByUnderLyingAssert(underLylingAssert);
		List<IntrumentDetails> intrumentDetails = setIntrumentDetails(instrumentList);
		String status = saveIntrumentDetails(intrumentDetails);
		return status;
	}

	private List<IntrumentDetails> setIntrumentDetails(List<Instrument> instrumentList) {
		return instrumentList.stream().map(intrument -> {
			return IntrumentDetails.builder().instrument_token(intrument.instrument_token)
					.tradingsymbol(intrument.tradingsymbol).name(intrument.name)
					.instrument_type(intrument.instrument_type).exchange(intrument.exchange).strike(intrument.strike)
					.lot_size(intrument.lot_size).expiry(intrument.expiry).build();
		}).collect(Collectors.toList());
	}

//	private LocalDateTime getLocalDateTime(Date date) {
//		
//		System.out.println("Date :"+date +" Year :"+(1900+date.getYear())+" "+"Month :"+date.getMonth()+" Day :"+date.getDate());
//		return LocalDateTime.of((1900+date.getYear()), date.getMonth(), date.getDate(), 00, 00, 00, 00);
//		
//		
//	}

}
