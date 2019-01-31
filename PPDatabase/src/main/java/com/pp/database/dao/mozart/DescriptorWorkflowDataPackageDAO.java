package com.pp.database.dao.mozart;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.pp.database.kernel.PPDAO;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;

@Repository
public class DescriptorWorkflowDataPackageDAO extends PPDAO<DescriptorWorkflowDataPackage>{

	public DescriptorWorkflowDataPackageDAO() {
		super(DescriptorWorkflowDataPackage.class);
	}
	
	public List<DescriptorWorkflowDataPackage> getBeforeDate(Long interval) {
		return this.createQuery().disableValidation().field("creationDate").lessThan(new Date(System.currentTimeMillis() - interval)).asList();
	}
	
}
