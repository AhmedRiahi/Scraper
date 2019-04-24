package com.pp.database.dao.engine;

import com.pp.database.kernel.PPDAO;
import com.pp.database.model.common.DescriptorsPortfolio;
import com.pp.database.model.engine.DescriptorJobDataSet;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DescriptorJobDataSetDAO extends PPDAO<DescriptorJobDataSet> {

    public DescriptorJobDataSetDAO(){
        super(DescriptorJobDataSet.class);
    }


    public Optional<DescriptorJobDataSet> findByPortfolioAndJobName(DescriptorsPortfolio descriptorsPortfolio,String jobName){
        List<DescriptorJobDataSet> descriptorJobDataSets = this.createQuery().field("descriptorsPortfolio").equal(descriptorsPortfolio).asList();
        return descriptorJobDataSets.stream().filter(descriptorJobDataSet -> descriptorJobDataSet.getJobName().equalsIgnoreCase(jobName)).findFirst();
    }
}
