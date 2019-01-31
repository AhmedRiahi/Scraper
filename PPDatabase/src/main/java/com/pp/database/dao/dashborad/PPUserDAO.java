package com.pp.database.dao.dashborad;

import org.springframework.stereotype.Repository;

import com.pp.database.kernel.PPDAO;
import com.pp.database.model.dashboard.PPUser;

@Repository
public class PPUserDAO extends PPDAO<PPUser>{

	public PPUserDAO() {
		super(PPUser.class);
	}

	
	public PPUser findByEmail(String email) {
		return this.createQuery().field("email").equal(email).get();
	}

}
