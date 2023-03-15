package com.algo.model;

import lombok.Builder;

@Builder
public class OptionDetailsOfStrikePrice {

	private String underLyingAssert;
	private String strikePrice;
	private String instrumentType;
	private double lastTradedPrice;
	private String symbol;
	private long token;
	private String premiumDiff;
	
	
	

	@Override
	public String toString() {
		return instrumentType+"==>"+lastTradedPrice+"     ";
	}
}
