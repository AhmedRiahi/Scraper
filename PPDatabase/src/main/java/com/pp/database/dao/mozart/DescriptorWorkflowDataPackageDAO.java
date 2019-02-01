package com.pp.database.dao.mozart;

import com.pp.database.kernel.PPDAO;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class DescriptorWorkflowDataPackageDAO extends PPDAO<DescriptorWorkflowDataPackage>{

	public DescriptorWorkflowDataPackageDAO() {
		super(DescriptorWorkflowDataPackage.class);
	}
	
	public List<DescriptorWorkflowDataPackage> getBeforeDate(Long interval) {
		return this.createQuery().disableValidation().field("creationDate").lessThan(new Date(System.currentTimeMillis() - interval)).asList();
	}
	
}
