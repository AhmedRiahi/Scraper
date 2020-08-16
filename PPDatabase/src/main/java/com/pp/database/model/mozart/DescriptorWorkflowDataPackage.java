package com.pp.database.model.mozart;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pp.database.kernel.PPEntity;
import com.pp.database.model.common.DescriptorsPortfolio;
import com.pp.database.model.crawler.CrawledContent;
import com.pp.database.model.engine.DescriptorJob;
import com.pp.database.model.scrapper.descriptor.listeners.ScrapedContent;
import com.pp.database.model.semantic.individual.PPIndividual;
import lombok.Data;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Entity
public class DescriptorWorkflowDataPackage extends PPEntity{

	@Reference
	private DescriptorsPortfolio portfolio;
	@JsonIgnore
	private DescriptorJob descriptorJob;
	@Reference
	private CrawledContent crawledContent;
	@Transient
	private Map<ScrapedContent,List<ScrapedContent>> scrapedContents;
	private List<ScrapedContent> allScrapedContents;
	private List<PPIndividual> individuals = new ArrayList<>();
    private List<String> generatedLinks;
	private Set<String> schemasNames;
	@Embedded
	private DWDPDebugInformation debugInformation = new DWDPDebugInformation();
	@Embedded
	private DWDPJoinDetails joinDetails = new DWDPJoinDetails();

	public List<PPIndividual> getValidIndividuals(){
		return this.individuals.stream().filter(PPIndividual::isValid).collect(Collectors.toList());
	}


}
