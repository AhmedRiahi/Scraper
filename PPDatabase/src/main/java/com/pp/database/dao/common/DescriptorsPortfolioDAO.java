package com.pp.database.dao.common;

import com.pp.database.kernel.PPDAO;
import com.pp.database.model.common.DescriptorsPortfolio;
import org.springframework.stereotype.Repository;

@Repository
public class DescriptorsPortfolioDAO extends PPDAO<DescriptorsPortfolio> {

    public DescriptorsPortfolioDAO(){
        super(DescriptorsPortfolio.class);
    }
}
