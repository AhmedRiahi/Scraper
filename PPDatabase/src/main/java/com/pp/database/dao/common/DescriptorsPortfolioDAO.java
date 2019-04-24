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
}
