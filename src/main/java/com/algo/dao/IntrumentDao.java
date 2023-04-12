package com.algo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.algo.model.IntrumentDetails;

@Repository
public interface IntrumentDao extends JpaRepository<IntrumentDetails, Integer> {

	
}
