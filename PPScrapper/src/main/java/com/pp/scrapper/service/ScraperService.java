package com.pp.scrapper.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.scrapper.core.PPHTMLScrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.database.model.scrapper.descriptor.DescriptorScrapingResult;
import com.pp.database.model.semantic.individual.PPIndividual;
import com.pp.scrapper.core.IndividualsBuilder;

@Service
public class ScraperService {

	@Autowired
	private IndividualsBuilder individualsBuilder;
	@Autowired
    private DescriptorWorkflowDataPackageDAO dwdpDAO;
	

	public void scrapDescriptor(DescriptorWorkflowDataPackage dwdp) throws IOException {
		PPHTMLScrapper htmlScraper = new PPHTMLScrapper(dwdp.getDescriptorJob().getDescriptor(), dwdp.getCrawledContent());
		DescriptorScrapingResult dsr = htmlScraper.scrapDescriptor();
        dsr.setDsmId(dwdp.getDescriptorJob().getDescriptorSemanticMappingId());
		dwdp.setAllScrapedContents(dsr.getAllScrapedContents());
		if(dwdp.getDescriptorJob().isGenerateLinks()){
		    List<String> links = htmlScraper.detectedAllLinks();
		    dwdp.setGeneratedLinks(links);
        }
        this.dwdpDAO.save(dwdp);
		List<PPIndividual> individuals = this.individualsBuilder.buildScrapedIndividuals(dsr);
		individuals.stream().forEach(individual -> {
			individual.setDescriptorId(dwdp.getDescriptorJob().getDescriptor().getStringId());
			individual.setWorkflowId(dwdp.getStringId());
		});
		dwdp.setIndividuals(individuals);
		dwdp.setScrapedContents(dsr.getScrapedContents());
	}

}
