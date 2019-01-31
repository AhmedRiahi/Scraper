package com.pp.database.model.mozart;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pp.database.model.common.DescriptorsPortfolio;
import com.pp.database.model.engine.DescriptorJob;
import lombok.Data;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;

import com.pp.database.kernel.PPEntity;
import com.pp.database.model.crawler.CrawledContent;
import com.pp.database.model.scrapper.descriptor.listeners.ScrapedContent;
import com.pp.database.model.semantic.individual.PPIndividual;

@Data
@Entity
public class DescriptorWorkflowDataPackage extends PPEntity{

	@Reference
	private DescriptorsPortfolio portfolio;
	private DescriptorJob descriptorJob;
	@Reference
	private CrawledContent crawledContent;
	@Transient
	private Map<ScrapedContent,List<ScrapedContent>> scrapedContents;
	@JsonIgnore
	private List<ScrapedContent> allScrapedContents;
    @JsonIgnore
	private List<PPIndividual> individuals;
    @JsonIgnore
    private List<String> generatedLinks;
	private Set<String> schemasNames;
	@Embedded
	private DWDPDebugInformation debugInformation = new DWDPDebugInformation();
	@Embedded
	private DWDPJoinDetails joinDetails = new DWDPJoinDetails();


}
