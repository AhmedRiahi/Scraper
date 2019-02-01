package com.pp.database.dao.mozart;

import com.pp.database.kernel.PPDAO;
import com.pp.database.model.mozart.JobExecutionHistory;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class JobExecutionHistoryDAO extends PPDAO<JobExecutionHistory>{

	public JobExecutionHistoryDAO() {
		super(JobExecutionHistory.class);
	}

	public JobExecutionHistory getByDWDPId(String dwdpId) {
		return this.createQuery().disableValidation().field("dwdp.$id").equal(new ObjectId(dwdpId)).get();
	}
	
	public List<JobExecutionHistory> getByDescriptorId(String descriptorId){
		return this.createQuery().disableValidation().field("descriptor.$id").equal(new ObjectId(descriptorId)).order("-startTime").limit(10).asList();
	}

	public List<JobExecutionHistory> getByPortfolioId(String portfolioId){
		return this.createQuery().disableValidation().field("portfolio.$id").equal(new ObjectId(portfolioId)).order("-startTime").limit(10).asList();
	}

    public JobExecutionHistory getByExecutionId(String executionId){
        return this.createQuery()
                .disableValidation()
                .field("_id")
                .equal(new ObjectId(executionId))
                .get();
    }

	public List<JobExecutionHistory> getBeforeDate(Long interval) {
		return this.createQuery().disableValidation().field("creationDate").lessThan(new Date(System.currentTimeMillis() - interval)).asList();
	}

    public List<JobExecutionHistory> getInError() {
        return this.createQuery().disableValidation().field("inError").equal(true).asList();
    }

    public List<JobExecutionHistory> getActiveJobs() {
        return this.createQuery().disableValidation().field("finishTime").doesNotExist().asList();
    }
}
