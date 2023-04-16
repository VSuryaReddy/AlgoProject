package com.algo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.algo.model.KiteToken;

@Repository
public interface TokenDao extends JpaRepository<KiteToken, Integer>{

	
}
