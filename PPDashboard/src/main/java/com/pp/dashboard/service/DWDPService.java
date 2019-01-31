package com.pp.dashboard.service;


import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.model.scrapper.descriptor.listeners.ScrapedContent;
import com.pp.database.model.semantic.individual.PPIndividual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DWDPService {

    @Autowired
    private DescriptorWorkflowDataPackageDAO dwdpDAO;

    public List<ScrapedContent> getScrapedContents(String dwdpId){
        return this.dwdpDAO.get(dwdpId).getAllScrapedContents();
    }

    public List<PPIndividual> getIndividuals(String dwdpId){
        return this.dwdpDAO.get(dwdpId).getIndividuals();
    }

}
