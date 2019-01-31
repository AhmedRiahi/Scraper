package com.pp.scrapper.core;

import java.util.ArrayList;
import java.util.List;

import com.pp.database.model.scrapper.descriptor.DescriptorSemanticMapping;
import org.springframework.stereotype.Service;

import com.pp.database.model.scrapper.descriptor.DescriptorModel;
import com.pp.database.model.scrapper.descriptor.DescriptorScrapingResult;
import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import com.pp.database.model.scrapper.descriptor.listeners.ScrapedContent;
import com.pp.database.model.scrapper.descriptor.relation.AggregationRelation;
import com.pp.database.model.scrapper.descriptor.relation.CompositionRelation;
import com.pp.database.model.scrapper.descriptor.relation.SemanticRelation;
import com.pp.database.model.scrapper.descriptor.relation.SemanticRelation.CardinalityType;
import com.pp.database.model.semantic.individual.IndividualProperty;
import com.pp.database.model.semantic.individual.PPIndividual;


@Service
public class IndividualsBuilder {

	public List<PPIndividual> buildScrapedIndividuals(DescriptorScrapingResult scrapingResult){
		List<PPIndividual> individuals = new ArrayList<>();
		List<ContentListenerModel> orderedContentListener = ScrapingUtils.generateOrderedContentListeners(scrapingResult.getDescriptor(),DescriptorModel.SEMANTIC_LISTENER);
		DescriptorSemanticMapping dsm = scrapingResult.getDescriptor().getSemanticMappingById(scrapingResult.getDsmId()).get();
		//IndividualSchema individualSchema = PPSemanticWorld.getInstance().getIndividualSchema(scrapingResult.getDescriptor().getSchemaName());
		for(ContentListenerModel cl : orderedContentListener){
			// Check if this is a semantic ContentListener
			if(cl.isIndividual() == true){
				List<ScrapedContent> scrapedContents = scrapingResult.getScrapedContentByCL(cl);
				int index = 0;
				
				for(ScrapedContent sc : scrapedContents){
					String clSemanticName = dsm.getClSemanticName(cl);
			        PPIndividual individual = new PPIndividual(clSemanticName);
			        List<SemanticRelation> semanticRelations = scrapingResult.getDescriptor().getSemanticRelationsAsSource(cl);
			        
			        for(SemanticRelation sr : semanticRelations){
			        	IndividualProperty propertyValue = new IndividualProperty();
			        	String propertyName = dsm.getClSemanticName(sr.getTarget());
		        		propertyValue.setName(propertyName);
		        		String targetValue = null;
			        	if(sr.getTarget().isStatic()) {
			        		//Handle static content listener
			        		targetValue = sr.getTarget().getStaticValue();
			        	}else {
			        		if(sr instanceof CompositionRelation){
				        		ScrapedContent itemScrapedContent = null;
				        		List<ScrapedContent> scTargets = scrapingResult.getChildsOfWithCL(sc, sr.getTarget().getName());
				        		if(scTargets==null || scTargets.size() == 0) {
				        			continue;
				        		}
				        		// TODO : Check cardinality type : index not always incremental
				        		if(sr.getCardinalityType().equals(CardinalityType.MANY_TO_ONE)){
				        			itemScrapedContent = scrapingResult.getScrapedContentByCL(sr.getTarget()).get(0);
				        		}else{
				        			if(sr.getCardinalityType().equals(CardinalityType.ONE_TO_ONE)){
				        				itemScrapedContent = scTargets.get(0);
				        			}else {
				        				//TODO implement other cardinalities
				        			}
				        		}
				        		// TODO Bad technique to check basing on Content Listener name
				        		if(sr.getTarget().getName().equalsIgnoreCase("link")) {
				        			targetValue = itemScrapedContent.getContent().attr("href");
				        		}else {
				        			targetValue = itemScrapedContent.getContent().text();
				        		}
				        		
				        	}else {
				        		if(sr instanceof AggregationRelation){
				        			//TODO implement aggregation relation process
				        		}
				        	}
			        	}
			        	propertyValue.setValue(targetValue);
						individual.addProperty(propertyValue);
			        }
			        
			        individuals.add(individual);
			        index++;
				}
			}
		}
		return individuals;
	}
	
	
}
