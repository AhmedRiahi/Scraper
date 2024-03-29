package com.pp.scrapper.service;

import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.model.crawler.CrawledContent;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.database.model.scrapper.descriptor.DescriptorScrapingResult;
import com.pp.database.model.scrapper.descriptor.DescriptorType;
import com.pp.database.model.semantic.individual.PPIndividual;
import com.pp.scrapper.core.IndividualsBuilder;
import com.pp.scrapper.core.PPHTMLScrapper;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ScraperService {

	@Autowired
	private IndividualsBuilder individualsBuilder;
	@Autowired
    private DescriptorWorkflowDataPackageDAO dwdpDAO;
	

	public void scrapDescriptor(DescriptorWorkflowDataPackage dwdp) throws IOException {
		CrawledContent crawledContent = dwdp.getCrawledContent();
		if(dwdp.getDescriptorJob().getDescriptor().getDescriptorType().equals(DescriptorType.JSON)){
			JSONObject json = new JSONObject(crawledContent.getContents());
			String xml = XML.toString(json,"body");
			crawledContent.setContents(xml);
		}
		PPHTMLScrapper htmlScraper = new PPHTMLScrapper(dwdp.getDescriptorJob().getDescriptor(), crawledContent);
		DescriptorScrapingResult dsr = htmlScraper.scrapDescriptor();
        dsr.setDsmId(dwdp.getDescriptorJob().getDescriptorSemanticMappingId());
		dwdp.setAllScrapedContents(dsr.getAllScrapedContents());
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
