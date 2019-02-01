package com.pp.database.dao.common;

import com.pp.database.kernel.PPDAO;
import com.pp.database.model.common.DescriptorsPortfolio;
import com.pp.database.model.engine.DescriptorJob;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DescriptorsPortfolioDAO extends PPDAO<DescriptorsPortfolio> {

    public DescriptorsPortfolioDAO(){
        super(DescriptorsPortfolio.class);
    }

    public List<DescriptorsPortfolio> findUnprocessedJobs(){
        Query<DescriptorsPortfolio> query = this.getDatastore().createQuery(DescriptorsPortfolio.class);
        Query<DescriptorJob> jobsQuery = this.getDatastore().createQuery(DescriptorJob.class);
        jobsQuery.where("lastCheckingDate < new Date() - (checkingInterval * 60 *1000)").and(jobsQuery.criteria("checkingRequired").equal(false));
        query.disableValidation().field("jobs").hasAnyOf(jobsQuery);
        return query.asList();
    }
}
