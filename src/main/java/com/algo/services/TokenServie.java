package com.algo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.algo.constants.UtilityClass;
import com.algo.dao.TokenDao;
import com.algo.model.KiteToken;

@Service
public class TokenServie {

	@Autowired
	TokenDao tokenDao;

	public KiteToken getKiteToken() {
		List<KiteToken> kiteTokenList = tokenDao.findAll();
		return !UtilityClass.isListEmpty(kiteTokenList) ? kiteTokenList.get(0) : null;
	}

	public void saveKiteToken(KiteToken kiteToken) {
		tokenDao.save(kiteToken);
	}

}
