package com.algo.services;

import java.util.Calendar;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class ExpiryDayOfComingWeek {

	public Date getExpiryDayOfComingWeek(int expiryDate) {
		Calendar date = Calendar.getInstance();
		int diff = expiryDate - date.get(Calendar.DAY_OF_WEEK);

		if (diff < 0) {
			diff += 7;
		}
		date.add(Calendar.DAY_OF_MONTH, diff);
//		date.setTimeInMillis(0); 
		Date reqDate = date.getTime();
//        DateUtils.setHours(reqDate, 00);
//        DateUtils.setMinutes(reqDate, 00);
//        DateUtils.setSeconds(reqDate, 00);
//        DateUtils.setMilliseconds(reqDate, 000);
        
	
		reqDate.setHours(0);
		reqDate.setMinutes(0);
		reqDate.setSeconds(0);

		return reqDate;
	}
	
}
