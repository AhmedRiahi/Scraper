package com.pp.database.dao.scrapper;

import java.util.List;

import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Repository;

import com.pp.database.kernel.PPDAO;
import com.pp.database.model.scrapper.descriptor.DescriptorModel;

@Repository
public class DescriptorDAO extends PPDAO<DescriptorModel>{

	public DescriptorDAO() {
		super(DescriptorModel.class);
	}
	
	public List<DescriptorModel> findUnprocessedDescriptors(){
		Query<DescriptorModel> query = this.createQuery();
		query.where("this.lastCheckingDate < new Date() - (this.checkingInterval * 60 *1000)").and(query.criteria("checkingRequired").equal(false));
		return query.asList();
	}
	
}
