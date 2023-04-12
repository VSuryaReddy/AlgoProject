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
	public long instrument_token;

	@Column(name = "EXCHANGE_TOKEN")
	public long exchange_token;

	@Column(name = "SYMBOL")
	public String tradingsymbol;

	@Column(name = "UNDER_LYNIG")
	public String name;

	@Column(name = "INTRUNEMT_TYPE")
	public String instrument_type;

	@Column(name = "EXCHANGE")
	public String exchange;

	@Column(name = "STRIKE_PRICE")
	public String strike;

	@Column(name = "LOT_SIZE")
	public int lot_size;

	@Column(name = "EXPIRY_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	public Date expiry;

}
