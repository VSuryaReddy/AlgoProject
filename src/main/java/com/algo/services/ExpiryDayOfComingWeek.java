package com.algo.services;

import java.util.Calendar;
import java.util.Date;
import java.text.ParseException;

import org.springframework.stereotype.Component;

import com.algo.constants.Constants;

@Component
public class ExpiryDayOfComingWeek {


	public Date getExpiryDayOfComingWeek(int expiryDate) throws ParseException {
		Calendar date = Calendar.getInstance();
		int diff = expiryDate - date.get(Calendar.DAY_OF_WEEK);
		if (diff < 0) {
			diff += 7;
		}
		date.add(Calendar.DAY_OF_MONTH, diff);
		Date reqDate = date.getTime();
		return Constants.sdfWithOutTime.parse(Constants.sdfWithOutTime.format(reqDate));
	}
}
