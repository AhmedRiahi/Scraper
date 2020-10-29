package com.pp.dashboard.service;


import com.pp.database.dao.common.DescriptorsPortfolioDAO;
import com.pp.database.model.common.DescriptorsPortfolio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DescriptorsPortfolioService {

    @Autowired
    private DescriptorsPortfolioDAO descriptorsPortfolioDAO;

    public void create(DescriptorsPortfolio descriptorsPortfolio){
        this.descriptorsPortfolioDAO.save(descriptorsPortfolio);
    }

    public List<DescriptorsPortfolio> getAll(){
        return this.descriptorsPortfolioDAO.find().asList();
    }

    public void delete(String portfolioId){
        this.descriptorsPortfolioDAO.delete(portfolioId);
    }

}
