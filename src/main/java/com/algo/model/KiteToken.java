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
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="KITE_TOKEN")
public class KiteToken {

	@Id
	@GeneratedValue
	private int id;

	@Column(name="TODAY_DATE")
	@Temporal(TemporalType.DATE)
	private Date todayDate;

	@Column(name = "ACCESS_TOKEN")
	private String accessToken;
	
	@Column(name = "PUBLIC_TOKEN")
	private String publicToken;


}
