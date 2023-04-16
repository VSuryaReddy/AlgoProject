package com.algo.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.algo.model.IntrumentDetails;

@Repository
public interface IntrumentDao extends JpaRepository<IntrumentDetails, Integer> {

	IntrumentDetails findByInstrumentToken(long instrumentToken);

	List<IntrumentDetails> findByExpiryDay(Date expiryDay);
	
	List<IntrumentDetails> findByName(String underLyingAssert);
	

	@Query(value="select * from intrument inst where inst.expiry_date = :expiry "
			+ "and inst.intrunemt_type =:instrumentType  and inst.under_lynig =:underLynigAssert and inst.strike_price in (:strikePriceList)",nativeQuery = true)
	List<IntrumentDetails> getIntDetByUnderAssertAndExpAndStrPriAndInstType(@Param("expiry") Date expiry,
			                                                                @Param("instrumentType") String instrumentType,
			                                                                @Param("underLynigAssert") String underLynigAssert,
			                                                                @Param("strikePriceList") List<String> strikePriceList);

	@Query(value="select * from intrument inst where inst.expiry_date = :expiry "
			+ "and inst.under_lynig =:underLynigAssert and inst.strike_price in (:strikePriceList)",nativeQuery = true)
	List<IntrumentDetails> getIntDetByUnderAssertAndExpAndStrPrice(@Param("expiry") Date expiry,
                                                                   @Param("underLynigAssert") String underLynigAssert,
                                                                   @Param("strikePriceList") List<String> strikePriceList);
	}
