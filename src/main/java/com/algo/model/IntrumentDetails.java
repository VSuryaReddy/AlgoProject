package com.algo.model;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@Builder
@Entity
@Table(name = "INTRUMENT")
@NoArgsConstructor
@AllArgsConstructor
public class IntrumentDetails {

	@Id
	@GeneratedValue
	public int id;
	
	@Column(name = "TOKEN")
	public long instrumentToken;

	@Column(name = "EXCHANGE_TOKEN")
	public long exchangeToken;

	@Column(name = "SYMBOL")
	public String tradingSymbol;

	@Column(name = "UNDER_LYNIG")
	public String name;

	@Column(name = "INTRUNEMT_TYPE")
	public String instrumentType;

	@Column(name = "EXCHANGE")
	public String exchange;

	@Column(name = "STRIKE_PRICE")
	public String strikePrice;

	@Column(name = "LOT_SIZE")
	public int lotSize;

	@Column(name = "EXPIRY_DATE")
	@Temporal(TemporalType.DATE)
	public Date expiryDay;

}
